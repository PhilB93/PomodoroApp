package com.example.pomodoro

fun stringTimeToLong(string: String): Long {
    val stringTime = string.split(":")
    var time = 0L
    for (i in 0..2) {
        when (i) {
            0 -> time += stringTime[0].toLong() * 1000L * 60L * 60L
            1 -> time += stringTime[1].toLong() * 1000L * 60L
            2 -> time += stringTime[1].toLong() * 1000L
        }
    }
    return time
}

fun longTimeToString(time: Long): String {
    return if (time <= 0L) START_TIME
    else "%02d:%02d:%02d".format(
        time / 1000 / 3600,
        time / 1000 % 3600 / 60,
        time / 1000 % 60
    )
}

const val START_TIME = "00:00:00"
const val INVALID = "INVALID"
const val COMMAND_START = "COMMAND_START"
const val COMMAND_STOP = "COMMAND_STOP"
const val COMMAND_ID = "COMMAND_ID"
const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME"
const val INTERVAL = 1000L