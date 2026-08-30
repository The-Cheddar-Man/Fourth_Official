package com.example.fourthofficial.domain.rules

import com.example.fourthofficial.domain.match.MatchPlayerState

fun canSubstituteOn(state: MatchPlayerState,
                    totalElapsedMs: Long, alreadyUsed: Boolean = false): Boolean {
    return !state.isOnField && !alreadyUsed &&
            !isYellowActive(state, totalElapsedMs) && !state.isRedCarded
}

fun canSubstituteOff(state: MatchPlayerState,
                     totalElapsedMs: Long, alreadyUsed: Boolean = false): Boolean {
    return state.isOnField && !alreadyUsed &&
            !state.isRedCarded && !isYellowActive(state, totalElapsedMs)
}