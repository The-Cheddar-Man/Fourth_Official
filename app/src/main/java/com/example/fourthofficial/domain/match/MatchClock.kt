package com.example.fourthofficial.domain.match

data class MatchClock(
    val isRunning: Boolean = false,
    val halfElapsedMs: Long = 0L,
    val totalElapsedMs: Long = 0L
)