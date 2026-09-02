package com.example.fourthofficial.ui.match

sealed interface SubstitutionPreparationUiState {
    data object SelectPlayers : SubstitutionPreparationUiState
    data object AssignSubstitutions : SubstitutionPreparationUiState
}