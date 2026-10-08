package com.example.fourthofficial.ui.match.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.PreparedSubstitution
import com.example.fourthofficial.domain.rules.EventEditResult
import com.example.fourthofficial.ui.common.AppAlertDialog
import com.example.fourthofficial.ui.common.TeamPanel
import com.example.fourthofficial.ui.theme.AppButtonShape
import com.example.fourthofficial.ui.theme.DestructiveRed
import com.example.fourthofficial.ui.theme.SuccessGreen
import com.example.fourthofficial.ui.viewmodel.MatchViewModel
import kotlinx.coroutines.launch

@Composable
fun SubstitutionPreparationContent(
    vm: MatchViewModel,
    teamId: TeamId,
    initialReplacementPlayerOffId: PlayerId? = null,
    onReturnToMatch: () -> Unit
) {
    var replacementSelectionFor by remember(teamId, initialReplacementPlayerOffId) {
        mutableStateOf(initialReplacementPlayerOffId?.let { playerOffId ->
            SubstitutionSelectionTarget(teamId = teamId, playerOffId = playerOffId) }
        )
    }
    var reasonSelectionFor by remember(teamId) {
        mutableStateOf<SubstitutionSelectionTarget?>(null)
    }
    var swipeErrorMessage by remember(teamId) { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Assign Substitutions",
            style = MaterialTheme.typography.titleMedium
        )

        swipeErrorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        val orderedTeams = listOf(vm.team1, vm.team2)

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            orderedTeams.forEach { team ->
                val substitutions = vm.getPreparedSubstitutions(team.id)

                SubstitutionAssignmentTeamSection(
                    vm = vm,
                    teamId = team.id,
                    teamName = team.name.ifBlank { "Team ${team.index}" },
                    substitutions = substitutions,

                    onChooseReplacement = { playerOffId ->
                        replacementSelectionFor =
                            SubstitutionSelectionTarget(
                                teamId = team.id,
                                playerOffId = playerOffId
                            )
                    },

                    onChooseReason = { playerOffId ->
                        reasonSelectionFor =
                            SubstitutionSelectionTarget(
                                teamId = team.id,
                                playerOffId = playerOffId
                            )
                    },

                    onCancelSubstitution = { playerOffId ->
                        swipeErrorMessage = null

                        vm.removePreparedSubstitution(
                            teamId = team.id,
                            playerOffId = playerOffId
                        )

                        if (vm.getPreparedSubstitutions(team.id).isEmpty()) {
                            vm.cancelPreparedSubstitutionBatch(team.id)
                        }

                        if (noPreparedSubstitutionsRemain(vm)) {
                            onReturnToMatch()
                        }
                    },

                    onSubmitSubstitution = { playerOffId ->
                        when (
                            val result =
                                vm.applyPreparedSubstitution(
                                    teamId = team.id,
                                    playerOffId = playerOffId
                                )
                        ) {
                            EventEditResult.Success -> {
                                swipeErrorMessage = null

                                if (noPreparedSubstitutionsRemain(vm)) {
                                    onReturnToMatch()
                                }
                                true
                            }

                            is EventEditResult.Failure -> {
                                swipeErrorMessage = result.message
                                false
                            }
                        }
                    },

                    onSubmitAll = {
                        vm.applyPreparedSubstitutionBatch(team.id)

                        if (noPreparedSubstitutionsRemain(vm)) {
                            onReturnToMatch()
                        }
                    },

                    modifier =
                        if (substitutions.isEmpty()) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.fillMaxWidth().weight(1f)
                        }
                )
            }
        }

        OutlinedButton(
            onClick = onReturnToMatch,
            modifier = Modifier.fillMaxWidth(),
            shape = AppButtonShape
        ) {
            Text("Return to Match")
        }
    }

    val replacementTarget = replacementSelectionFor
    val reasonTarget = reasonSelectionFor

    if (reasonTarget != null) {
        SubstituteReasonDialogue(
            onConfirm = { substitutionType ->
                vm.setPreparedSubstitutionType(
                    teamId = reasonTarget.teamId,
                    playerOffId = reasonTarget.playerOffId,
                    type = substitutionType
                )

                reasonSelectionFor = null
            },
            onDismiss = {
                reasonSelectionFor = null
            }
        )
    }

    if (replacementTarget != null) {
        val selectionTeam =
            when (replacementTarget.teamId) {
                vm.team1.id -> vm.team1
                vm.team2.id -> vm.team2
                else -> null
            }

        val selectionBatch = vm.getPreparedSubstitutionBatch(replacementTarget.teamId)

        if (selectionTeam != null && selectionBatch != null) {
            val substitution = selectionBatch.substitutions.find {
                it.playerOffId == replacementTarget.playerOffId
            }
            val currentPlayerOn = substitution?.playerOnId?.let {
                    playerOnId -> selectionTeam.players.find { it.id == playerOnId }
            }

            val eligiblePlayers = (vm.eligiblePlayersOn(replacementTarget.teamId) +
                    listOfNotNull(currentPlayerOn))
                .distinctBy { player -> player.id.value }
                .sortedBy { player -> player.number }

            if (eligiblePlayers.isNotEmpty()) {
                val playerOff = selectionTeam.players.find { it.id == replacementTarget.playerOffId }

                val playerOffLabel = playerOff?.let {
                        player -> "${player.number}. " + player.name.ifBlank { "(Unnamed)" }
                } ?: "Unknown player"

                SubstitutePlayerOnDialogue(
                    playerOffLabel = playerOffLabel,
                    potentialSubs = eligiblePlayers,
                    onConfirm = { playerOnId ->
                        vm.setPreparedSubstitutionPlayerOn(
                            teamId = replacementTarget.teamId,
                            playerOffId = replacementTarget.playerOffId,
                            playerOnId = playerOnId
                        )

                        replacementSelectionFor = null
                        reasonSelectionFor = replacementTarget
                    },

                    onDismiss = { replacementSelectionFor = null }
                )
            } else {
                AppAlertDialog(
                    title = "Substitution",
                    onDismissRequest = { replacementSelectionFor = null },
                    text = { Text("No eligible replacement players are available.") },
                    confirmButton = {
                        Button(
                            onClick = { replacementSelectionFor = null },
                            shape = AppButtonShape
                        ) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SubstitutionAssignmentTeamSection(
    vm: MatchViewModel,
    teamId: TeamId,
    teamName: String,
    substitutions: List<PreparedSubstitution>,
    onChooseReplacement: (PlayerId) -> Unit,
    onChooseReason: (PlayerId) -> Unit,
    onCancelSubstitution: (PlayerId) -> Unit,
    onSubmitSubstitution: (PlayerId) -> Boolean,
    onSubmitAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allAssignmentsComplete = substitutions.isNotEmpty() &&
            substitutions.all { substitution ->
                substitution.playerOnId != null && substitution.type != null
            }

    val title =
        if (substitutions.isEmpty())
            teamName
        else
            "$teamName (${substitutions.size})"

    TeamPanel(
        title = title,
        headerContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            OutlinedButton(
                onClick = onSubmitAll,
                enabled = allAssignmentsComplete,
                shape = AppButtonShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
            ) {
                Text(
                    text = "Submit",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        modifier = modifier
    ) {
        if (substitutions.isEmpty()) {
            Text(
                text = "No substitutions prepared",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
        } else {
            SubstitutionAssignmentList(
                vm = vm,
                teamId = teamId,
                substitutions = substitutions,
                onChooseReplacement = onChooseReplacement,
                onChooseReason = onChooseReason,
                onCancelSubstitution = onCancelSubstitution,
                onSubmitSubstitution = onSubmitSubstitution,
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun SubstitutionAssignmentList(
    vm: MatchViewModel,
    teamId: TeamId,
    substitutions: List<PreparedSubstitution>,
    onChooseReplacement: (PlayerId) -> Unit,
    onChooseReason: (PlayerId) -> Unit,
    onCancelSubstitution: (PlayerId) -> Unit,
    onSubmitSubstitution: (PlayerId) -> Boolean,
    modifier: Modifier = Modifier
) {
    val team = when (teamId) {
        vm.team1.id -> vm.team1
        vm.team2.id -> vm.team2
        else -> return
    }

    val playersById = team.players.associateBy { player -> player.id }

    val playerStates = when (teamId) {
        vm.team1.id -> vm.team1PlayerStates
        vm.team2.id -> vm.team2PlayerStates
        else -> return
    }

    val orderedSubstitutions =
        substitutions.sortedBy { substitution ->
            playerStates[substitution.playerOffId]?.fieldPos ?: Int.MAX_VALUE
        }

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp))
    {
        items(
            items = orderedSubstitutions,
            key = { substitution -> substitution.playerOffId.value }
        ) { substitution ->
            val playerOff = playersById[substitution.playerOffId]
            val playerOn = substitution.playerOnId?.let { playerOnId -> playersById[playerOnId] }
            val assignmentComplete = substitution.playerOnId != null && substitution.type != null

            SwipeableSubstitutionAssignmentRow(
                canSubmit = assignmentComplete,
                onCancel = { onCancelSubstitution(substitution.playerOffId) },
                onSubmit = { onSubmitSubstitution(substitution.playerOffId) }
            ) {
                SubstitutionAssignmentRow(
                    playerOffLabel =
                        playerOff?.let { player ->
                            "${player.number}. " +
                                    player.name.ifBlank { "(Unnamed)" } } ?: "Unknown player",

                    playerOnLabel =
                        playerOn?.let { player ->
                            "${player.number}. " + player.name.ifBlank { "(Unnamed)" }
                        },

                    reasonLabel = substitution.type?.label,
                    onChooseReplacement = { onChooseReplacement(substitution.playerOffId) },
                    onChooseReason = { onChooseReason(substitution.playerOffId) }
                )
            }
        }
    }
}

@Composable
private fun SwipeableSubstitutionAssignmentRow(
    canSubmit: Boolean,
    onCancel: () -> Unit,
    onSubmit: () -> Boolean,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = canSubmit,
        enableDismissFromEndToStart = true,
        onDismiss = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    val success = onSubmit()
                    if (!success) {
                        scope.launch { dismissState.reset() }
                    }
                }
                SwipeToDismissBoxValue.EndToStart -> { onCancel() }
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val targetColor =
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> SuccessGreen
                    SwipeToDismissBoxValue.EndToStart -> DestructiveRed
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surface
                }

            val backgroundColor =
                lerp(
                    MaterialTheme.colorScheme.surface,
                    targetColor,
                    dismissState.progress.coerceIn(0f, 1f)
                )

            Box(
                modifier = Modifier.fillMaxSize().background(backgroundColor).padding(horizontal = 16.dp),
                contentAlignment =
                    when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        SwipeToDismissBoxValue.Settled -> Alignment.Center
                    }
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Text(
                            text = "SUBMIT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        Text(
                            text = "CANCEL",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    SwipeToDismissBoxValue.Settled -> Unit
                }
            }
        }
    ) {
        content()
    }
}

@Composable
private fun SubstitutionAssignmentRow(
    playerOffLabel: String,
    playerOnLabel: String?,
    reasonLabel: String?,
    onChooseReplacement: () -> Unit,
    onChooseReason: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = playerOffLabel, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onChooseReplacement, modifier = Modifier.weight(1f), shape = AppButtonShape)
            { Text(playerOnLabel ?: "Choose replacement") }

            OutlinedButton(onClick = onChooseReason, modifier = Modifier.weight(1f), shape = AppButtonShape)
            { Text(reasonLabel ?: "Choose reason")
            }
        }
    }
}

private data class SubstitutionSelectionTarget(val teamId: TeamId, val playerOffId: PlayerId)

private fun noPreparedSubstitutionsRemain(vm: MatchViewModel): Boolean {
    return vm.getPreparedSubstitutions(vm.team1.id).isEmpty() &&
            vm.getPreparedSubstitutions(vm.team2.id).isEmpty()
}
