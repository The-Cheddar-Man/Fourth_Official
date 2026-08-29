package com.example.fourthofficial.domain.rules

import com.example.fourthofficial.domain.match.MatchPlayerState

fun canSubstituteOn(state: MatchPlayerState,
                    totalElapsedMs: Long, alreadyUsed: Boolean = false): Boolean {
    return !state.isOnField && !alreadyUsed &&
            !isYellowActive(state, totalElapsedMs) && !isRedActive(state)
}

fun canSubstituteOff(state: MatchPlayerState,
                     totalElapsedMs: Long, alreadyUsed: Boolean = false): Boolean {
    return state.isOnField && !alreadyUsed &&
            !isRedActive(state) && !isYellowActive(state, totalElapsedMs)
}