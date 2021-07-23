package com.example.pomodoro


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.timerData.Timer
import com.example.pomodoro.timerData.TimerAdapter
import com.example.pomodoro.timerData.TimerListener


class MainActivity : AppCompatActivity(), TimerListener, LifecycleObserver {
    private lateinit var binding: ActivityMainBinding
    private val timerAdapter = TimerAdapter(this)
    private var nextId = 0
    private val timers = mutableListOf<Timer>()
    private var currentFinishTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

            recycler.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = timerAdapter
            }

            bntAddTimer.setOnClickListener {

                addNewTimer()
            }
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this, R.style.AlertDialog).apply {
            setTitle("Quit the application?")
            setPositiveButton("Yes") { _, _ ->
                currentFinishTime = 0L
                super.onBackPressed()
            }
            setNegativeButton("No") { _, _ ->
            }
            setCancelable(true)
        }.create().show()
    }

    private fun addNewTimer() {
        if (timers.size <= 100) {

            val hours: Int? = binding.enterHoursEditText.text.toString().toIntOrNull()
            val minutes: Int? = binding.enterMinutesEditText.text.toString().toIntOrNull()
            val seconds: Int? = binding.enterSecondsEditText.text.toString().toIntOrNull()
            if (hours != null && minutes != null && seconds != null)
                if (hours != 0 || minutes != 0 || seconds != 0) {
                    val milliSeconds: Long = (hours * 3600 + minutes * 60 + seconds) * 1000L
                    timers.add(Timer(nextId++, milliSeconds, false))
                    timerAdapter.submitList(timers.toList())
                }

        }
    }

    override fun start(id: Int) {
        timers.map {
            if (it.isStarted) stop(it.id, it.leftMS)
        }
        val index = timers.indexOf(timers.find { it.id == id })
        println(index)
        if (index >= 0) {
            timers[index].run {
                isStarted = true
                endTime = System.currentTimeMillis() + leftMS
                currentFinishTime = endTime
            }
        } else {
            println(index)
        }
        timerAdapter.submitList(timers.toList())

    }

    override fun stop(id: Int, currentMs: Long) {
        val index = timers.indexOf(timers.find { it.id == id })
        if (index>=0) {
            timers[index].run {
                leftMS = if (isWorked) startMs
                else endTime - System.currentTimeMillis()
                isStarted = false
            }
            currentFinishTime = 0L
        }else{
            println(index)
        }
        timerAdapter.submitList(timers.toList())
    }

    override fun delete(id: Int) {
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, currentFinishTime)
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        onAppForegrounded()
    }
}