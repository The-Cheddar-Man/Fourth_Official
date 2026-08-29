package com.example.fourthofficial.domain.rules

import com.example.fourthofficial.domain.match.MatchClock
import com.example.fourthofficial.domain.match.MatchPhase
import com.example.fourthofficial.domain.match.MatchPlayerState

fun isMatchInPlay(phase: MatchPhase, clock: MatchClock): Boolean {
    return clock.isRunning &&
            (phase == MatchPhase.FIRST_HALF || phase == MatchPhase.SECOND_HALF)
}

fun canActOnPlayer(state: MatchPlayerState, phase: MatchPhase, clock: MatchClock): Boolean {
    return state.isOnField && isMatchInPlay(phase, clock) &&
            !isYellowActive(state, clock.totalElapsedMs) && !isRedActive(state)
}

fun canFinishHalf(phase: MatchPhase, halfElapsedMs: Long, halfDurationMs: Long): Boolean {
    return (phase == MatchPhase.FIRST_HALF || phase == MatchPhase.SECOND_HALF) &&
            halfElapsedMs >= halfDurationMs
}