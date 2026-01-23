package com.example.fourthofficial.ui.components
import com.example.fourthofficial.model.DiscType

sealed interface MatchScreenUiState {
    data object None : MatchScreenUiState

    data class ActionMenu(val teamIndex: Int, val playerNumber: Int) : MatchScreenUiState

    data class ScorePick(val teamIndex: Int, val playerNumber: Int) : MatchScreenUiState

    data class SubPickOnPlayer(val teamIndex: Int, val offNumber: Int) : MatchScreenUiState
    data class SubPickReason(val teamIndex: Int, val offNumber: Int, val onNumber: Int) : MatchScreenUiState

    data class DiscPickType(val teamIndex: Int, val playerNumber: Int) : MatchScreenUiState
    data class DiscPickReason(val teamIndex: Int, val playerNumber: Int, val type: DiscType) : MatchScreenUiState
}