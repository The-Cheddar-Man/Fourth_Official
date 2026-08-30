package com.example.fourthofficial.domain.rules

import com.example.fourthofficial.domain.event.DisciplineReason
import com.example.fourthofficial.domain.event.DisciplineReasonRed
import com.example.fourthofficial.domain.event.DisciplineReasonYellow
import com.example.fourthofficial.domain.event.DisciplineType
import com.example.fourthofficial.domain.match.MatchPlayerState

const val YELLOW_DURATION_MS = 10L * 60L * 1000L

fun isYellowActive(state: MatchPlayerState, totalElapsedMs: Long): Boolean {
    val until = state.yellowUntilPlayingMs ?: return false
    return totalElapsedMs < until
}

fun isDisciplineReasonValid(type: DisciplineType, reason: DisciplineReason): Boolean {
    return when (type) {
        DisciplineType.YELLOW -> reason is DisciplineReasonYellow
        DisciplineType.RED -> reason is DisciplineReasonRed
    }
}

fun isSecondYellowCard(type: DisciplineType, hasPreviousYellow: Boolean): Boolean {
    return type == DisciplineType.YELLOW && hasPreviousYellow
}

fun applyYellowCard(state: MatchPlayerState, totalElapsedMs: Long): MatchPlayerState {
    return state.copy(
        yellowUntilPlayingMs = totalElapsedMs + YELLOW_DURATION_MS
    )
}

fun applyRedCard(state: MatchPlayerState): MatchPlayerState {
    return state.copy(
        isRedCarded = true,
        yellowUntilPlayingMs = null
    )
}

fun yellowRemainingMs(state: MatchPlayerState, totalElapsedMs: Long): Long {
    val until = state.yellowUntilPlayingMs ?: return 0L
    return (until - totalElapsedMs).coerceAtLeast(0L)
}