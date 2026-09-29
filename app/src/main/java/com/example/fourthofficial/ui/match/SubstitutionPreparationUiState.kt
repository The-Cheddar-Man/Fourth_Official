package com.example.fourthofficial.ui.match

import com.example.fourthofficial.domain.id.PlayerId

sealed interface SubstitutionPreparationUiState {
    data object SelectPlayers : SubstitutionPreparationUiState
    data class AssignSubstitutions(val initialReplacementPlayerOffId: PlayerId? = null) : SubstitutionPreparationUiState
}