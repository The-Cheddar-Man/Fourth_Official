package com.example.fourthofficial.ui.match

import com.example.fourthofficial.domain.event.DisciplineType
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId

sealed interface MatchScreenUiState {
    data object None : MatchScreenUiState
    data class ActionMenu(val teamId: TeamId, val playerId: PlayerId,
                          val eventTimeMs: Long, val halfIndex: Int) : MatchScreenUiState
    data class ScorePick(val teamId: TeamId, val playerId: PlayerId,
                         val eventTimeMs: Long, val halfIndex: Int) : MatchScreenUiState
    data class PreparingSubstitutions(
        val preparationState: SubstitutionPreparationUiState
    ) : MatchScreenUiState
    data class DiscPickType(val teamId: TeamId, val playerId: PlayerId, val
    eventTimeMs: Long, val halfIndex: Int) : MatchScreenUiState
    data class DiscPickReason(
        val teamId: TeamId, val playerId: PlayerId, val type: DisciplineType,
        val eventTimeMs: Long, val halfIndex: Int) : MatchScreenUiState
}