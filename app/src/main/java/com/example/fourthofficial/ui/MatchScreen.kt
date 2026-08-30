package com.example.fourthofficial.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchPhase
import com.example.fourthofficial.domain.match.MatchPlayerState
import com.example.fourthofficial.domain.event.DisciplineReason
import com.example.fourthofficial.domain.event.DisciplineReasonRed
import com.example.fourthofficial.domain.event.DisciplineReasonYellow
import com.example.fourthofficial.domain.event.DisciplineType
import com.example.fourthofficial.domain.match.PreparedSubstitution
import com.example.fourthofficial.domain.team.Player
import com.example.fourthofficial.domain.event.ScoreType
import com.example.fourthofficial.domain.event.SubstitutionType
import com.example.fourthofficial.domain.team.Team
import com.example.fourthofficial.ui.components.MatchScreenUiState
import com.example.fourthofficial.ui.components.SubstitutionPreparationUiState
import com.example.fourthofficial.ui.components.SingleChoiceDialog
import com.example.fourthofficial.ui.components.SubstitutionBatchReviewDialog
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

enum class ClockDisplayMode {
    RUGBY,
    HALF,
    TOTAL
}

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
    var clockDisplayMode by remember { mutableStateOf(ClockDisplayMode.RUGBY) }

    val elapsedToDisplay = when (clockDisplayMode) {
        ClockDisplayMode.RUGBY -> vm.displayElapsedMs
        ClockDisplayMode.HALF -> vm.clock.halfElapsedMs
        ClockDisplayMode.TOTAL -> vm.totalDisplayElapsedMs
    }

    val selectedTeam = { teamId: TeamId -> when(teamId){
        vm.team1.id -> vm.team1
        vm.team2.id -> vm.team2
        else -> error("Unknown TeamID: $teamId")
    }}
    val selectedTeamName = { teamId: TeamId ->
        val team = selectedTeam(teamId)
        team.name.ifBlank { "Team ${team.index}" } }
    val selectedPlayer = { teamId: TeamId, playerId: PlayerId ->
        selectedTeam(teamId).players.find { it.id == playerId }?.name?.ifBlank { "(Unnamed)" }
            ?: "(Unnamed)"
    }
    val playerLabel = { teamId: TeamId, playerId: PlayerId ->
        selectedTeam(teamId).players
            .find { it.id == playerId }?.let { player ->
                "${player.number}. ${player.name.ifBlank { "(Unnamed)" }}"
            }?: "Unknown player"
    }
    //endregion

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    )
    {
        //region Timers
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                when (clockDisplayMode){
                    ClockDisplayMode.RUGBY -> "Elapsed Time"
                    ClockDisplayMode.HALF -> "Half Elapsed Time"
                    ClockDisplayMode.TOTAL -> "Total Elapsed Time"
                },
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "Remaining Time",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                vm.formatClock(elapsedToDisplay, false),
                modifier = Modifier.weight(1f).clickable {
                    clockDisplayMode = when (clockDisplayMode) {
                        ClockDisplayMode.RUGBY -> ClockDisplayMode.HALF
                        ClockDisplayMode.HALF -> ClockDisplayMode.TOTAL
                        ClockDisplayMode.TOTAL -> ClockDisplayMode.RUGBY
                    }},
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                vm.formatClock(vm.halfRemainingMs, true),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayMedium
            )
        }
        //endregion

        //region Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(onClick = { vm.toggleClock() }, modifier = Modifier.weight(1f)) {
                Text(if (vm.clock.isRunning) "Stop clock" else "Start clock")
            }
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Start New Match", textAlign = TextAlign.Center)
            }
            Button(
                onClick = { showLogHalfDialog = true },
                enabled = vm.phase != MatchPhase.FINISHED,
                modifier = Modifier.weight(1f)
            ) {
                Text(when (vm.phase) {
                        MatchPhase.SECOND_HALF -> "End Match"
                        MatchPhase.FINISHED -> "Match Finished"
                        else -> "Log Half"
                    }
                )
            }
        }
        //endregion

        //region Scores
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.scoreForTeam(vm.team1.id).toString()
            )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = "Score"
            )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.scoreForTeam(vm.team2.id).toString()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.halfTimeScoreForTeam(vm.team1.id).toString()
            )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = "Score (HT)"
            )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.halfTimeScoreForTeam(vm.team2.id).toString()
            )
        }
        //endregion

        //region Team columns
        Row {
            TeamColumn(
                team = vm.team1,
                modifier = Modifier.weight(1f),
                vm = vm,
                playerStates = vm.team1PlayerStates,
                onPlayerTapped = { playerId ->
                    uiState = MatchScreenUiState.ActionMenu(vm.team1.id, playerId)
                }
            )
            TeamColumn(
                team = vm.team2,
                modifier = Modifier.weight(1f),
                vm = vm,
                playerStates = vm.team2PlayerStates,
                onPlayerTapped = { playerId ->
                    uiState = MatchScreenUiState.ActionMenu(vm.team2.id, playerId)
                }
            )
        }
        //endregion
    }

    when (val state = uiState) {

        MatchScreenUiState.None -> Unit

        is MatchScreenUiState.ActionMenu -> {
            Dialog(onDismissRequest = dismissDialogue) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .padding(24.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            "Select Action For ${
                                selectedPlayer(
                                    state.teamId,
                                    state.playerId
                                )
                            }"
                        )

                        Button(
                            onClick = {
                                uiState = MatchScreenUiState.ScorePick(
                                    state.teamId,
                                    state.playerId
                                )
                            }
                        ) {
                            Text("Score")
                        }

                        Button(
                            onClick = {
                                vm.startPreparedSubstitutionBatch(state.teamId)
                                vm.addPreparedSubstitution(state.playerId)

                                uiState = MatchScreenUiState.PreparingSubstitutions(
                                    SubstitutionPreparationUiState.PickPlayerOn(
                                        playerOffId = state.playerId
                                    )
                                )
                            }
                        ) {
                            Text("Substitution")
                        }

                        Button(
                            onClick = {
                                uiState = MatchScreenUiState.DiscPickType(
                                    state.teamId,
                                    state.playerId
                                )
                            }
                        ) {
                            Text("Discipline")
                        }
                    }
                }
            }
        }

        is MatchScreenUiState.ScorePick -> {
            ScoreDialogue(
                teamName = selectedTeamName(state.teamId),
                playerName = selectedPlayer(state.teamId, state.playerId),
                onConfirm = { scoreType ->
                    vm.recordScore(state.teamId, state.playerId, scoreType)
                    dismissDialogue()
                },
                onDismiss = dismissDialogue
            )
        }

        is MatchScreenUiState.PreparingSubstitutions -> {
            val batch = vm.preparedSubstitutionBatch

            if (batch != null) {
                when (val preparationState = state.preparationState) {
                    is SubstitutionPreparationUiState.PickPlayerOn -> {
                        val teamId = batch.teamId
                        val eligibleOn = vm.eligiblePlayersOn(teamId)
                        if (eligibleOn.isEmpty()) {
                            AlertDialog(
                                onDismissRequest = {
                                    vm.removePreparedSubstitution(preparationState.playerOffId)
                                    if (vm.getPreparedSubstitutions().isEmpty()) {
                                        dismissDialogue()
                                    }
                                    else {
                                        uiState = MatchScreenUiState.PreparingSubstitutions(
                                            SubstitutionPreparationUiState.Review)
                                    }
                                },
                                title = { Text("Substitutions") },
                                text = { Text("No players available for substitution.") },
                                confirmButton = {
                                    OutlinedButton(onClick = {
                                        vm.removePreparedSubstitution(preparationState.playerOffId)
                                        if (vm.getPreparedSubstitutions().isEmpty()) {
                                            dismissDialogue()
                                        }
                                        else {
                                            uiState = MatchScreenUiState.PreparingSubstitutions(
                                                SubstitutionPreparationUiState.Review)
                                        }
                                    }
                                )
                                { Text("Ok") } },
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

                                uiState =
                                    MatchScreenUiState.PreparingSubstitutions(
                                        SubstitutionPreparationUiState.PickReason(
                                            playerOffId = preparationState.playerOffId)
                                    )
                                },
                                onDismiss = {
                                    vm.removePreparedSubstitution(
                                        preparationState.playerOffId
                                    )

                                    if (vm.getPreparedSubstitutions().isEmpty()) {
                                        dismissDialogue()
                                    }
                                    else {
                                        uiState = MatchScreenUiState.PreparingSubstitutions(
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
                                uiState = MatchScreenUiState.PreparingSubstitutions(
                                    SubstitutionPreparationUiState.Review
                                )
                            },
                            onDismiss = {
                                vm.removePreparedSubstitution(preparationState.playerOffId)
                                if (vm.getPreparedSubstitutions().isEmpty()) {
                                    dismissDialogue()
                                } else {
                                    uiState = MatchScreenUiState.PreparingSubstitutions(
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
                                dismissDialogue()
                            },
                            onCancel = {
                                vm.cancelPreparedSubstitutionBatch()
                                dismissDialogue()
                            },
                            onAddAnother = {
                                uiState = MatchScreenUiState.PreparingSubstitutions(
                                    SubstitutionPreparationUiState.PickPlayerOff)
                            },
                            onRemove = { playerOffId ->
                                vm.removePreparedSubstitution(playerOffId)

                                if (vm.getPreparedSubstitutions().isEmpty()) {
                                    dismissDialogue()
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
                            AlertDialog(onDismissRequest = { uiState =
                                    MatchScreenUiState.PreparingSubstitutions(
                                        SubstitutionPreparationUiState.Review)
                                },
                                title = { Text("Substitutions") },
                                text = { Text("No players available for substitution.") },
                                confirmButton = { OutlinedButton(
                                    onClick = { uiState =
                                        MatchScreenUiState.PreparingSubstitutions(
                                            SubstitutionPreparationUiState.Review)
                                    })
                                { Text("Ok") } },
                                dismissButton = {}
                            )
                        }
                        else {
                            SubstitutePlayerOffDialogue(
                                potentialPlayers = eligiblePlayersOff,
                                onConfirm = { playerOffId ->
                                    vm.addPreparedSubstitution(playerOffId)
                                    uiState = MatchScreenUiState.PreparingSubstitutions(
                                        SubstitutionPreparationUiState.PickPlayerOn(
                                            playerOffId))
                                },
                                onDismiss = {
                                    uiState = MatchScreenUiState.PreparingSubstitutions(
                                        SubstitutionPreparationUiState.Review
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        is MatchScreenUiState.DiscPickType -> {
            DisciplineTypeDialogue(
                playerName = selectedPlayer(state.teamId, state.playerId),
                onConfirm = { discType ->
                    uiState = MatchScreenUiState.DiscPickReason(
                        teamId = state.teamId,
                        playerId = state.playerId,
                        type = discType
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
                                reason
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
                                reason
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
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Start New Match") },
            text = {
                Text("Are you sure you want to start a new match?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.resetClock()
                        vm.resetScores()
                        vm.resetSubstitutions()
                        vm.resetDiscs()
                        vm.resetPlayerStates()
                        showResetDialog = false
                    }
                ) {
                    Text("Yes, New Game")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showLogHalfDialog) {
        val canFinishHalf = vm.canFinishHalf
        val finishingMatch = vm.phase == MatchPhase.SECOND_HALF

        AlertDialog(
            onDismissRequest = { showLogHalfDialog = false },
            title = { Text(if (finishingMatch) "End Match" else "Log Half") },
            text = {
                Text(when {
                    !canFinishHalf ->
                        "This half is not over!"
                    finishingMatch ->
                        "End the match?"
                    else ->
                        "Log the first half?"
                })
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (vm.phase) {
                            MatchPhase.FIRST_HALF -> vm.logHalf()
                            MatchPhase.SECOND_HALF -> vm.endMatch()
                            else -> Unit
                        }
                        showLogHalfDialog = false
                    },
                    enabled = canFinishHalf
                ) {
                    Text(if (finishingMatch) "End Match" else "Log Half")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogHalfDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

//region Composables
@Composable
fun ScoreDialogue(
    teamName: String,
    playerName: String,
    onConfirm: (ScoreType) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: ScoreType? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Scoring",
        prompt = "$playerName ($teamName) scored:",
        options = ScoreType.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun SubstitutePlayerOnDialogue(
    playerOffLabel: String,
    potentialSubs: List<Player>,
    onConfirm: (PlayerId) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: Player? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Substitution",
        prompt = "Substitute $playerOffLabel for:",
        options = potentialSubs,
        selected = selected,
        optionLabel = { player -> "${player.number}. ${player.name.ifBlank { "(Unnamed)" }}" },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it.id) },
        onDismiss = onDismiss
    )
}

@Composable
fun SubstituteReasonDialogue(
    onConfirm: (SubstitutionType) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: SubstitutionType? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Substitution",
        prompt = "Reason for substitution:",
        options = SubstitutionType.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun SubstituteSummaryDialogue(
    substitutions: List<PreparedSubstitution>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onAddAnother: () -> Unit,
    onRemove: (PlayerId) -> Unit,
    labelForSubstitution: (PreparedSubstitution) -> String,
    canAddAnotherSubstitution: Boolean
) {
    SubstitutionBatchReviewDialog(substitutions, labelForSubstitution, onRemove, onAddAnother, onConfirm, onCancel, canAddAnotherSubstitution)
}

@Composable
fun SubstitutePlayerOffDialogue(
    potentialPlayers: List<Player>,
    onConfirm: (PlayerId) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: Player? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Substitution",
        prompt = "Player coming off:",
        options = potentialPlayers,
        selected = selected,
        optionLabel = { player -> "${player.number}. ${player.name.ifBlank { "(Unnamed)" }}" },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it.id) },
        onDismiss = onDismiss
    )
}

@Composable
fun DisciplineTypeDialogue(
    playerName: String,
    onConfirm: (DisciplineType) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: DisciplineType? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Discipline",
        prompt = "Card for $playerName:",
        options = DisciplineType.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun DisciplineReasonYellowDialogue(
    onConfirm: (DisciplineReason) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: DisciplineReason? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Discipline",
        prompt = "Discipline reason:",
        options = DisciplineReasonYellow.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun DisciplineReasonRedDialogue(
    onConfirm: (DisciplineReason) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: DisciplineReason? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Discipline",
        prompt = "Discipline reason:",
        options = DisciplineReasonRed.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
private fun playerTileColor(yellowActive: Boolean, redActive: Boolean) = when {
    redActive -> Color(0xFFE74751)
    yellowActive -> Color(0xFFFFB834)
    else -> MaterialTheme.colorScheme.surface
}

@Composable
fun TeamColumn(
    team: Team, modifier: Modifier = Modifier, vm: MatchViewModel,
    playerStates: Map<PlayerId, MatchPlayerState>, onPlayerTapped: (PlayerId) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(5.dp)
    )
    {
        val onField = team.players.mapNotNull { player ->
                playerStates[player.id]?.let { state -> player to state }
            }.filter { (_, state) ->
                state.isOnField
            }.sortedBy { (_, state) ->
                state.fieldPos ?: 999
            }

        LazyColumn {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(team.name.ifBlank { "Team ${team.index}" }, textAlign = TextAlign.Center)
                }
            }
            items(onField.size) { i ->
                val (player, state) = onField[i]
                val locked = !vm.canActOnPlayer(state)
                Surface(
                    color = playerTileColor(vm.isYellowActive(state), state.isRedCarded),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .then(
                            if (!locked) Modifier.clickable { onPlayerTapped(player.id) }
                            else Modifier
                        )
                )
                {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                    {
                        Text("${player.number}. ${player.name}")

                        if (vm.isYellowActive(state)) {
                            Text("Yellow: ${
                                vm.formatClock(vm.yellowRemainingMs(state),false)}"
                            )
                        }
                        if (state.isRedCarded) {
                            Text("Red")
                        }
                    }
                }
            }
        }
    }
}
//endregion

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun MatchScreenPreview() {
    MatchScreen(vm = MatchViewModel())
}