package com.example.fourthofficial.domain.rules

import com.example.fourthofficial.domain.match.MatchClock
import com.example.fourthofficial.domain.match.MatchPhase
import com.example.fourthofficial.domain.match.MatchPlayerState

fun isActiveHalf(phase: MatchPhase): Boolean {
    return phase == MatchPhase.FIRST_HALF || phase == MatchPhase.SECOND_HALF
}

fun canActOnPlayer(state: MatchPlayerState, phase: MatchPhase, clock: MatchClock): Boolean {
    return state.isOnField && isActiveHalf(phase) &&
            !isYellowActive(state, clock.totalElapsedMs) && !state.isRedCarded
}

fun hasReachedHalfDuration(phase: MatchPhase, halfElapsedMs: Long, halfDurationMs: Long): Boolean {
    return (phase == MatchPhase.FIRST_HALF || phase == MatchPhase.SECOND_HALF) &&
            halfElapsedMs >= halfDurationMs
}