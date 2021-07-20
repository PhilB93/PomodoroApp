package com.example.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.TimerItemBinding

class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {
    private var timer: CountDownTimer? = null
    private var totalMS = 0L

    fun bind(timer: Timer) {
        binding.timerTextView.text = timer.currentMS.displayTime()

        when {
            mapOfStartedTimersId.containsKey(timer.id) -> {
                totalMS = mapOfStartedTimersId[timer.id]?.first!!
                binding.tomatoProgress.setCurrent(totalMS - timer.currentMS)
//                TODO("Destroy dangerous with NullPointerException")
            }
            else -> {
                totalMS = timer.currentMS
                mapOfStartedTimersId[timer.id] = Triple(timer.currentMS, totalMS, false)
            }
        }

        binding.tomatoProgress.setPeriod(totalMS)

        if (totalMS != timer.currentMS && timer.isStarted)
            binding.tomatoProgress.setCurrent(timer.currentMS)

        if (timer.currentMS == 0L)
            offTimerElements(timer.id)

        when {
            timer.isStarted -> startTimer(timer)
            else -> stopTimer()
        }

        initButtonsListeners(timer)
//        TODO("Fix bag with blinking circle")
    }

    private fun initButtonsListeners(timer: Timer) {
        binding.startPauseButton.setOnClickListener {
            if (!mapOfStartedTimersId[timer.id]?.third!!) {
                when {
                    timer.isStarted -> {
                        listener.stop(timer.id, timer.currentMS)
                    }
                    else -> {
                        TimerAdapter.Start.startedPosition = timer.id
                        listener.start(timer.id)
                    }
                }
            }
        }

        binding.deleteButton.setOnClickListener {
            binding.tomatoProgress.setCurrent(0)
            changeBackgroundOfItem(R.color.transparent)
            mapOfStartedTimersId.remove(timer.id)
            listener.delete(timer.id)
        }
    }

    private fun startTimer(timer: Timer) {
        binding.startPauseButton.text = resources.getString(R.string.stop)

        this.timer?.cancel()
        this.timer = getCountDownTimer(timer)
        this.timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer() {
        binding.startPauseButton.text = resources.getString(R.string.start)

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer =
        object : CountDownTimer(PERIOD, UNIT_ONE_HUNDRED_MS) {
            override fun onTick(millisUntilFinished: Long) {
                if (TimerAdapter.Start.startedPosition == timer.id)
                    interactOnTimer(timer)
                else
                    listener.stop(timer.id, timer.currentMS)
            }

            override fun onFinish() {}
        }

    private fun interactOnTimer(timer: Timer){
        if (timer.currentMS == 0L) {
            offTimerElements(timer.id)
            if (!mapOfStartedTimersId[timer.id]?.third!!)
            stopTimer()

            timer.isStarted = false
            mapOfStartedTimersId[timer.id] = Triple(timer.currentMS, totalMS, !timer.isStarted)
        } else {
            timer.currentMS -= UNIT_ONE_HUNDRED_MS
            binding.tomatoProgress.setCurrent(totalMS - timer.currentMS)
            binding.timerTextView.text = timer.currentMS.displayTime()
        }
    }

    private fun changeBackgroundOfItem(color: Int) =
        binding.box.setBackgroundColor(ResourcesCompat.getColor(resources, color, null))

    private fun offTimerElements(id: Int){
        changeBackgroundOfItem(R.color.tomato_dark)
        binding.timerTextView.text = mapOfStartedTimersId[id]?.second?.displayTime()
        binding.tomatoProgress.setCurrent(0)
    }



    private companion object {
        private const val UNIT_ONE_HUNDRED_MS = 100L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
    }
}