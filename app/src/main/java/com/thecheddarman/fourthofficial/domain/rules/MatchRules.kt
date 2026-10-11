package com.thecheddarman.fourthofficial.domain.rules

import com.thecheddarman.fourthofficial.domain.match.MatchClock
import com.thecheddarman.fourthofficial.domain.match.MatchPhase
import com.thecheddarman.fourthofficial.domain.match.MatchPlayerState

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