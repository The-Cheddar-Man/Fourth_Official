package com.example.fourthofficial.ui.match.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.ui.match.SubstitutionPreparationUiState
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

@Composable
fun SubstitutionPreparationContent(
    vm: MatchViewModel,
    preparationState: SubstitutionPreparationUiState,
    playerLabel: (TeamId, PlayerId) -> String,
    onPreparationStateChange: (SubstitutionPreparationUiState) -> Unit,
    onExit: () -> Unit
) {
    val batch = vm.preparedSubstitutionBatch ?: return

    when (preparationState) {
        is SubstitutionPreparationUiState.PickPlayerOn -> {
            val teamId = batch.teamId
            val eligibleOn = vm.eligiblePlayersOn(teamId)
            if (eligibleOn.isEmpty()) {
                AlertDialog(
                    onDismissRequest = {
                        vm.removePreparedSubstitution(preparationState.playerOffId)
                        if (vm.getPreparedSubstitutions().isEmpty()) {
                            onExit()
                        }
                        else {
                            onPreparationStateChange(
                                SubstitutionPreparationUiState.Review)
                        }
                    },
                    title = { Text("Substitutions") },
                    text = { Text("No players available for substitution.") },
                    confirmButton = {
                        OutlinedButton(onClick = {
                            vm.removePreparedSubstitution(preparationState.playerOffId)
                            if (vm.getPreparedSubstitutions().isEmpty()) {
                                onExit()
                            }
                            else {
                                onPreparationStateChange(
                                    SubstitutionPreparationUiState.Review)
                            }
                        }
                        )
                        { Text("Ok") }
                    },
                    dismissButton = {}
                )
            }
            else {
                SubstitutePlayerOnDialogue(
                    playerOffLabel = playerLabel(teamId, preparationState.playerOffId),
                    potentialSubs = eligibleOn,
                    onConfirm = { playerOnId -> vm.setPreparedSubstitutionPlayerOn(
                        playerOffId = preparationState.playerOffId,
                        playerOnId = playerOnId
                    )
                        onPreparationStateChange(
                            SubstitutionPreparationUiState.PickReason(
                                playerOffId = preparationState.playerOffId))
                    },
                    onDismiss = {
                        vm.removePreparedSubstitution(
                            preparationState.playerOffId
                        )

                        if (vm.getPreparedSubstitutions().isEmpty()) {
                            onExit()
                        }
                        else {
                            onPreparationStateChange(
                                SubstitutionPreparationUiState.Review)
                        }
                    }
                )
            }
        }

        is SubstitutionPreparationUiState.PickReason -> {
            SubstituteReasonDialogue(
                onConfirm = { substitutionType -> vm.setPreparedSubstitutionType(
                    playerOffId = preparationState.playerOffId,
                    type = substitutionType
                )
                    onPreparationStateChange(
                        SubstitutionPreparationUiState.Review)
                },
                onDismiss = {
                    vm.removePreparedSubstitution(preparationState.playerOffId)
                    if (vm.getPreparedSubstitutions().isEmpty()) {
                        onExit()
                    }
                    else {
                        onPreparationStateChange(
                            SubstitutionPreparationUiState.Review)
                    }
                }
            )
        }

        is SubstitutionPreparationUiState.Review -> {
            val teamId = batch.teamId
            val canAddAnotherSubstitution = vm.canAddAnotherSubstitution(teamId)

            SubstituteSummaryDialogue(
                substitutions = vm.getPreparedSubstitutions(),
                onConfirm = {
                    vm.applyPreparedSubstitutionBatch()
                    onExit()
                },
                onCancel = {
                    vm.cancelPreparedSubstitutionBatch()
                    onExit()
                },
                onAddAnother = {
                    onPreparationStateChange(
                        SubstitutionPreparationUiState.PickPlayerOff)
                },
                onRemove = { playerOffId ->
                    vm.removePreparedSubstitution(playerOffId)

                    if (vm.getPreparedSubstitutions().isEmpty()) {
                        onExit()
                    }
                },
                labelForSubstitution = { substitution ->
                    val playerOnId = substitution.playerOnId
                    val type = substitution.type

                    if (playerOnId != null && type != null) {
                        "${playerLabel(teamId, substitution.playerOffId)} → " +
                                "${playerLabel(teamId, playerOnId)} (${type.label})"
                    }
                    else {
                        "${playerLabel(teamId, substitution.playerOffId)} → Not assigned"
                    }
                },
                canAddAnotherSubstitution =
                    canAddAnotherSubstitution
            )
        }

        is SubstitutionPreparationUiState.PickPlayerOff -> {
            val teamId = batch.teamId
            val eligiblePlayersOff = vm.eligiblePlayersOff(teamId)
            if (eligiblePlayersOff.isEmpty()) {
                AlertDialog(
                    onDismissRequest = {
                        onPreparationStateChange(
                            SubstitutionPreparationUiState.Review)
                    },
                    title = { Text("Substitutions") },
                    text = { Text("No players available for substitution.") },
                    confirmButton = {
                        OutlinedButton(
                            onClick = {
                                onPreparationStateChange(
                                    SubstitutionPreparationUiState.Review)
                            }
                        )
                        { Text("Ok") }
                    },
                    dismissButton = {}
                )
            }
            else {
                SubstitutePlayerOffDialogue(
                    potentialPlayers = eligiblePlayersOff,
                    onConfirm = { playerOffId ->
                        vm.addPreparedSubstitution(playerOffId)
                        onPreparationStateChange(
                            SubstitutionPreparationUiState.PickPlayerOn(playerOffId))
                    },
                    onDismiss = { onPreparationStateChange(
                        SubstitutionPreparationUiState.Review)
                    }
                )
            }
        }
    }
}