package com.example.pomodoro.timerData
data class Timer(
    val id: Int,
    var startMs: Long,
    var isStarted: Boolean,
    var leftMS: Long = startMs,
    var endTime: Long = 0L,
    var isWorked:Boolean = false
)