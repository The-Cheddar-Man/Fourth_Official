package com.thecheddarman.fourthofficial.domain.rules

import com.thecheddarman.fourthofficial.domain.event.Substitution
import com.thecheddarman.fourthofficial.domain.event.SubstitutionType
import com.thecheddarman.fourthofficial.domain.id.PlayerId
import com.thecheddarman.fourthofficial.domain.id.TeamId
import com.thecheddarman.fourthofficial.domain.match.MatchPhase
import com.thecheddarman.fourthofficial.domain.match.MatchPlayerState

fun allowsSubstitutions(phase: MatchPhase): Boolean {
    return phase == MatchPhase.FIRST_HALF ||
            phase == MatchPhase.HALF_TIME ||
            phase == MatchPhase.SECOND_HALF
}

fun canSubstituteOn(state: MatchPlayerState, totalElapsedMs: Long,
                    alreadyUsed: Boolean = false, canReturn: Boolean): Boolean {
    return !state.isOnField && !alreadyUsed && canReturn &&
            !isYellowActive(state, totalElapsedMs) && !state.isRedCarded
}

fun canSubstituteOff(state: MatchPlayerState,
                     totalElapsedMs: Long, alreadyUsed: Boolean = false): Boolean {
    return state.isOnField && !alreadyUsed &&
            !state.isRedCarded && !isYellowActive(state, totalElapsedMs)
}

fun canReturn(events: List<Substitution>, teamId: TeamId, playerId: PlayerId): Boolean {
    return events.none { event -> event.teamId == teamId &&
            event.playerOffId == playerId && event.type == SubstitutionType.INJURY }
}