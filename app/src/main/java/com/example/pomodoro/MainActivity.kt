package com.example.pomodoro


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding

import org.greenrobot.eventbus.Subscribe

class MainActivity : AppCompatActivity(), TimerListener, LifecycleObserver {
    private lateinit var binding: ActivityMainBinding

    private val timerAdapter = TimerAdapter(this)
    private val timers = arrayListOf<Timer>()
    private var nextId = 0
    private var timerMillis = 1000L
    private var timerId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            nextId = savedInstanceState.getInt(NEXT_ID)
            val size = savedInstanceState.getInt(SIZE_TIMERS)
            for (i in 0 until size) {
                val id = savedInstanceState.getInt("$ID$i")
                val currentMs = savedInstanceState.getLong("$MS$i")
                val isStarted = savedInstanceState.getBoolean("$START$i")
                timers.add(Timer(id, currentMs, isStarted))
            }
            timerAdapter.submitList(timers.toList())
        }

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            if (timers.size <= 1000) {
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
    }


    override fun start(id: Int) {
        changeTimer(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeTimer(id, currentMs, false)
    }

    override fun delete(id: Int) {
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())
    }

    private fun changeTimer(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Timer>()
        timers.forEach {
            newTimers.add(
                if (it.id == id) Timer(it.id, currentMs ?: it.currentMS, isStarted)
                else it
            )
        }
        timerAdapter.submitList(newTimers)
        timers.clear()
        timers.addAll(newTimers)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        for (i in timers)
        {
            if (i.isStarted) {
                timerMillis = i.currentMS
                timerId = i.id
            }
        }
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.run {
            putExtra(COMMAND_ID, COMMAND_START)
            putExtra(STARTED_TIMER_TIME_MS, timerMillis)
            putExtra(NEXT_ID, timerId)
        }
        startService(startIntent)

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(NEXT_ID, nextId)
        outState.putInt(SIZE_TIMERS, timers.size)
        for (i in timers.indices) {
            outState.putInt("$ID$i", timers[i].id)
            outState.putLong("$MS$i", timers[i].currentMS)
            outState.putBoolean("$START$i", timers[i].isStarted)
        }
    }

    private companion object {
        const val NEXT_ID = "nextID"
        const val ID = "id"
        const val MS = "ms"
        const val START = "start"
        const val SIZE_TIMERS = "size of timers"
    }
}