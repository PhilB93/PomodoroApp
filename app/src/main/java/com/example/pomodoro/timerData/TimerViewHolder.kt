package com.example.pomodoro.timerData

import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.INTERVAL
import com.example.pomodoro.databinding.TimerItemBinding
import com.example.pomodoro.longTimeToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener
) :
    RecyclerView.ViewHolder(binding.root) {

    private var countDownTimer: CountDownTimer? = null

    fun bind(timer: Timer) {
//Забинди и дай ифы
        binding.stopwatchTimer.text = longTimeToString(timer.leftMS)

        isVisibleCheck(true)


        if (timer.isWorked) finish(timer)
        else {
            binding.customTimer.setPeriod(timer.startMs)
            CoroutineScope(Dispatchers.Main).launch {
                binding.customTimer.setCurrent(timer.leftMS)
                delay(INTERVAL)
            }

            if (timer.isStarted) {
                startTimer(timer)

            } else stopTimer()
        }
        initButtonsListeners(timer)
    }

    //стартуй и учти видимость
    private fun startTimer(timer: Timer) {
        binding.bntStartStop.text = STOP

        if (timer.isWorked) {
            timer.isWorked = false
            isVisibleCheck(true)
        }

        countDownTimer?.cancel()
        countDownTimer = getCountDownTimer(timer)
        countDownTimer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    //стопуй и учти видимость
    private fun stopTimer() {
        binding.bntStartStop.text = START

        countDownTimer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer {

        return object : CountDownTimer(timer.leftMS, INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                if (timer.isStarted) {
                    if (timer.leftMS <= 0L) {
                        onFinish()
                    } else {
                        timer.leftMS = timer.endTime - System.currentTimeMillis()
                        binding.stopwatchTimer.text = longTimeToString(timer.leftMS)
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.customTimer.setCurrent(timer.leftMS)
                            delay(INTERVAL)
                        }
                    }
                } else {
                    stopTimer()
                }
            }

            override fun onFinish() {
                finish(timer)
            }
        }
    }
//проверка н авидимость
    private fun isVisibleCheck(visible: Boolean) {
        if (visible) {
            binding.layout.setBackgroundColor(Color.TRANSPARENT)
        } else {
            binding.layout.setBackgroundColor(Color.GREEN)
        }
        binding.customTimer.isInvisible = !visible
    }

    private fun finish(timer: Timer) {
        countDownTimer?.cancel()

        binding.run {
            bntStartStop.text = START
            stopwatchTimer.text = longTimeToString(timer.startMs)
            blinkingIndicator.isInvisible = true
            (blinkingIndicator.background as? AnimationDrawable)?.stop()
        }

        timer.run {
            leftMS = timer.startMs
            isStarted = false
            isWorked = true
        }
        listener.stop(timer.id, timer.startMs)
        isVisibleCheck(false)
    }


    private fun initButtonsListeners(timer: Timer) {
        binding.bntStartStop.setOnClickListener {
            if (timer.isStarted) {
                binding.bntStartStop.text = START
                listener.stop(timer.id, timer.leftMS)
            } else {
                startTimer(timer)
                listener.start(timer.id)
            }
        }

        binding.btnDelete.setOnClickListener {
            binding.bntStartStop.isClickable = false
            listener.delete(timer.id)
        }
    }


    private companion object {
        private const val START = "START"
        private const val STOP = "STOP"
    }
}