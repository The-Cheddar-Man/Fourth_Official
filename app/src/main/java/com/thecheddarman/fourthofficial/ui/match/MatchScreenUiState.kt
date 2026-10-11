package com.thecheddarman.fourthofficial.ui.match

import com.thecheddarman.fourthofficial.domain.event.DisciplineType
import com.thecheddarman.fourthofficial.domain.id.PlayerId
import com.thecheddarman.fourthofficial.domain.id.TeamId

sealed interface MatchScreenUiState {
    data object None : MatchScreenUiState
    data class ActionMenu(val teamId: TeamId, val playerId: PlayerId,
                          val eventTimeMs: Long, val halfIndex: Int) : MatchScreenUiState
    data class ScorePick(val teamId: TeamId, val playerId: PlayerId,
                         val eventTimeMs: Long, val halfIndex: Int) : MatchScreenUiState
    data class PreparingSubstitutions(
        val teamId: TeamId,
        val initialReplacementPlayerOffId: PlayerId? = null
    ) : MatchScreenUiState
    data class DiscPickType(val teamId: TeamId, val playerId: PlayerId, val
    eventTimeMs: Long, val halfIndex: Int) : MatchScreenUiState
    data class DiscPickReason(
        val teamId: TeamId, val playerId: PlayerId, val type: DisciplineType,
        val eventTimeMs: Long, val halfIndex: Int) : MatchScreenUiState
}