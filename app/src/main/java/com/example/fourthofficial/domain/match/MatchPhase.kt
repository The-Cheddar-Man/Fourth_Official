package com.example.fourthofficial.domain.match

enum class MatchPhase {
    NOT_STARTED,
    FIRST_HALF,
    HALF_TIME,
    SECOND_HALF,
    FINISHED
}

data class MatchClock(
    val isRunning: Boolean = false,
    val halfElapsedMs: Long = 0L,
    val totalElapsedMs: Long = 0L
)