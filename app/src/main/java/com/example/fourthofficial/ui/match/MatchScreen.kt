package com.example.fourthofficial.ui.match

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fourthofficial.domain.event.DisciplineType
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchPhase
import com.example.fourthofficial.ui.match.components.ActionMenuDialogue
import com.example.fourthofficial.ui.match.components.DisciplineReasonRedDialogue
import com.example.fourthofficial.ui.match.components.DisciplineReasonYellowDialogue
import com.example.fourthofficial.ui.match.components.DisciplineTypeDialogue
import com.example.fourthofficial.ui.match.components.FinishHalfDialogue
import com.example.fourthofficial.ui.match.components.MatchContent
import com.example.fourthofficial.ui.match.components.MatchTeamColumns
import com.example.fourthofficial.ui.match.components.ScoreDialogue
import com.example.fourthofficial.ui.match.components.StartNewMatchDialogue
import com.example.fourthofficial.ui.match.components.SubstitutionPreparationContent
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

@Composable
fun MatchScreen(
    modifier: Modifier = Modifier,
    vm: MatchViewModel
) {
    //region vars and vals
    var uiState by remember { mutableStateOf<MatchScreenUiState>(MatchScreenUiState.None) }
    val dismissDialogue = { uiState = MatchScreenUiState.None }
    var showResetDialog by remember { mutableStateOf(false) }
    var showLogHalfDialog by remember { mutableStateOf(false) }

    val selectedTeam = { teamId: TeamId -> when(teamId){
        vm.team1.id -> vm.team1
        vm.team2.id -> vm.team2
        else -> error("Unknown TeamID: $teamId")
    }}
    val selectedTeamName = { teamId: TeamId ->
        val team = selectedTeam(teamId)
        team.name.ifBlank { "Team ${team.index}" }
    }
    val selectedPlayer = { teamId: TeamId, playerId: PlayerId ->
        selectedTeam(teamId).players.find { it.id == playerId }?.name?.ifBlank { "(Unnamed)" }
            ?: "(Unnamed)"
    }
    //endregion

    MatchContent(
        modifier = modifier,
        vm = vm,
        onStartNewMatchRequested = { showResetDialog = true },
        onFinishHalfRequested = { showLogHalfDialog = true }
    )
    {
        val currentUiState = uiState

        if (currentUiState is MatchScreenUiState.PreparingSubstitutions) {
            SubstitutionPreparationContent(
                vm = vm,
                teamId = currentUiState.teamId,
                teamName = selectedTeamName(currentUiState.teamId),
                preparationState = currentUiState.preparationState,
                onPreparationStateChange = { preparationState ->
                    uiState = MatchScreenUiState.PreparingSubstitutions(
                        teamId = currentUiState.teamId,
                        preparationState = preparationState
                    )
                },
                onReturnToMatch = {
                    if (vm.getPreparedSubstitutions(currentUiState.teamId).isEmpty()) {
                        vm.cancelPreparedSubstitutionBatch(currentUiState.teamId)
                    }
                    uiState = MatchScreenUiState.None
                                  },
                onDiscard = {
                    vm.cancelPreparedSubstitutionBatch(currentUiState.teamId)
                    uiState = MatchScreenUiState.None
                }
            )
        } else {
            MatchTeamColumns(
                vm = vm,
                onPlayerTapped = { teamId, playerId ->
                    uiState = MatchScreenUiState.ActionMenu(
                        teamId = teamId,
                        playerId = playerId,
                        eventTimeMs = vm.displayElapsedMs,
                        halfIndex = vm.currentHalf
                    )
                },

                onPlayerLongPressed = { teamId, playerId ->
                    vm.startPreparedSubstitutionBatch(teamId)
                    vm.addPreparedSubstitution(teamId = teamId, playerOffId = playerId)

                    uiState = MatchScreenUiState.PreparingSubstitutions(
                            teamId = teamId,
                            preparationState = SubstitutionPreparationUiState.SelectPlayers
                        )
                },

                onPreparedSubstitutionsTapped = { teamId ->
                    uiState = MatchScreenUiState.PreparingSubstitutions(
                            teamId = teamId,
                            preparationState = SubstitutionPreparationUiState.SelectPlayers
                        )
                }
            )
        }
    }

    when (val state = uiState) {

        MatchScreenUiState.None -> Unit

        is MatchScreenUiState.ActionMenu -> {
            ActionMenuDialogue(
                playerName = selectedPlayer(state.teamId, state.playerId),
                onScore = {
                    uiState = MatchScreenUiState.ScorePick(
                        state.teamId,
                        state.playerId,
                        state.eventTimeMs,
                        state.halfIndex
                    )
                },
                onSubstitution = {
                    vm.startPreparedSubstitutionBatch(state.teamId)
                    vm.addPreparedSubstitution(state.teamId, state.playerId)

                    uiState = MatchScreenUiState.PreparingSubstitutions(
                        teamId = state.teamId,
                        preparationState = SubstitutionPreparationUiState.SelectPlayers
                    )
                },
                onDiscipline = {
                    uiState = MatchScreenUiState.DiscPickType(
                        state.teamId,
                        state.playerId,
                        state.eventTimeMs,
                        state.halfIndex
                    )
                },
                onDismiss = dismissDialogue
            )
        }

        is MatchScreenUiState.ScorePick -> {
            ScoreDialogue(
                teamName = selectedTeamName(state.teamId),
                playerName = selectedPlayer(state.teamId, state.playerId),
                onConfirm = { scoreType ->
                    vm.recordScore(
                        state.teamId, state.playerId, scoreType, state.eventTimeMs, state.halfIndex)
                    dismissDialogue()
                },
                onDismiss = dismissDialogue
            )
        }

        is MatchScreenUiState.PreparingSubstitutions -> Unit

        is MatchScreenUiState.DiscPickType -> {
            DisciplineTypeDialogue(
                playerName = selectedPlayer(state.teamId, state.playerId),
                onConfirm = { discType ->
                    uiState = MatchScreenUiState.DiscPickReason(
                        teamId = state.teamId,
                        playerId = state.playerId,
                        type = discType,
                        state.eventTimeMs,
                        state.halfIndex
                    )
                },
                onDismiss = dismissDialogue
            )
        }

        is MatchScreenUiState.DiscPickReason -> {
            when (state.type) {
                DisciplineType.YELLOW -> {
                    DisciplineReasonYellowDialogue(
                        onConfirm = { reason ->
                            vm.recordDiscipline(
                                state.teamId,
                                state.playerId,
                                state.type,
                                reason,
                                state.eventTimeMs,
                                state.halfIndex
                            )
                            dismissDialogue()
                        },
                        onDismiss = dismissDialogue
                    )
                }

                DisciplineType.RED -> {
                    DisciplineReasonRedDialogue(
                        onConfirm = { reason ->
                            vm.recordDiscipline(
                                state.teamId,
                                state.playerId,
                                state.type,
                                reason,
                                state.eventTimeMs,
                                state.halfIndex
                            )
                            dismissDialogue()
                        },
                        onDismiss = dismissDialogue
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        StartNewMatchDialogue(
            onConfirm = {
                vm.resetClock()
                vm.resetScores()
                vm.resetSubstitutions()
                vm.resetDiscs()
                vm.resetPlayerStates()

                uiState = MatchScreenUiState.None
                showResetDialog = false
            },
            onDismiss = {
                showResetDialog = false
            }
        )
    }

    if (showLogHalfDialog) {
        val canFinishHalf = vm.canFinishHalf
        val finishingMatch = vm.phase == MatchPhase.SECOND_HALF

        FinishHalfDialogue(
            canFinishHalf = canFinishHalf,
            finishingMatch = finishingMatch,
            onConfirm = {
                when (vm.phase) {
                    MatchPhase.FIRST_HALF -> vm.logHalf()
                    MatchPhase.SECOND_HALF -> vm.endMatch()
                    else -> Unit
                }
                showLogHalfDialog = false
            },
            onDismiss = {
                showLogHalfDialog = false
            }
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun MatchScreenPreview() {
    MatchScreen(vm = MatchViewModel())
}