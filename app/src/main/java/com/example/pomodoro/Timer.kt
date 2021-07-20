package com.example.pomodoro

data class Timer(
    val id: Int,
    var currentMS: Long,
    var isStarted: Boolean
)