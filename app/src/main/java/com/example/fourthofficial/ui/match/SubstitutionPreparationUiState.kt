package com.example.fourthofficial.ui.match

import com.example.fourthofficial.domain.id.PlayerId

sealed interface SubstitutionPreparationUiState {

    data class PickPlayerOn(val playerOffId: PlayerId) : SubstitutionPreparationUiState
    data class PickReason(val playerOffId: PlayerId) : SubstitutionPreparationUiState
    data object Review : SubstitutionPreparationUiState
    data object PickPlayerOff : SubstitutionPreparationUiState
}