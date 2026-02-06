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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fourthofficial.model.DiscReason
import com.example.fourthofficial.model.DiscReasonRed
import com.example.fourthofficial.model.DiscReasonYellow
import com.example.fourthofficial.model.DiscType
import com.example.fourthofficial.model.GetScoreTypePoints
import com.example.fourthofficial.model.Player
import com.example.fourthofficial.model.ScoreType
import com.example.fourthofficial.model.SubType
import com.example.fourthofficial.model.Team
import com.example.fourthofficial.ui.components.MatchScreenUiState
import com.example.fourthofficial.ui.components.SingleChoiceDialog
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

@Composable
fun MatchScreen(modifier: Modifier = Modifier,
                vm: MatchViewModel
) {
    var uiState by remember { mutableStateOf<MatchScreenUiState>(MatchScreenUiState.None) }
    val dismissDialogue = { uiState = MatchScreenUiState.None }
    var showResetDialog by remember { mutableStateOf(false) }
    var showLogHalfDialog by remember { mutableStateOf(false) }

    val selectedTeam = { teamIndex: Int -> if (teamIndex == 1) vm.team1 else vm.team2 }
    val selectedTeamName = { teamIndex: Int -> selectedTeam(teamIndex).name.ifBlank { "Team $teamIndex" } }
    val selectedPlayer = { teamIndex: Int, playerNum: Int ->
        selectedTeam(teamIndex).players[playerNum - 1].name.ifBlank { "(Unnamed)" }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                "Elapsed Time",
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
                vm.formatClock(vm.halfElapsedMs, false),
                modifier = Modifier.weight(1f),
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(onClick = { vm.toggleClock() }, modifier = Modifier.weight(1f)) {
            Text(if (vm.clock.isRunning) "Stop clock" else "Start clock")
            }
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.weight(1f)) {
                Text("Start New Match", textAlign = TextAlign.Center)
            }
            Button(
                onClick = { showLogHalfDialog = true },
                modifier = Modifier.weight(1f)) {
                Text( "Log Half")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.scoreEvents.filter { it.teamIndex == 1 }
                    .sumOf { GetScoreTypePoints(it.type) }.toString() )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = "Score" )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.scoreEvents.filter { it.teamIndex == 2 }
                    .sumOf { GetScoreTypePoints(it.type) }.toString() )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.scoreEvents.filter { it.teamIndex == 1 && it.halfIndex == 1 }
                    .sumOf { GetScoreTypePoints(it.type) }.toString() )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = "Score (HT)" )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.scoreEvents.filter { it.teamIndex == 2 && it.halfIndex == 1 }
                    .sumOf { GetScoreTypePoints(it.type) }.toString() )
        }

        Row {
            TeamColumn(
                team = vm.team1,
                modifier = Modifier.weight(1f),
                vm = vm,
                isYellowActive = { vm.isYellowActive(it) },
                isRedActive = { vm.isRedActive(it) },
                yellowLabel = { player -> vm.formatClock(vm.yellowRemainingMs(player), false) },
                onPlayerTapped = { number ->
                    uiState = MatchScreenUiState.ActionMenu(1, number)
                }
            )
            TeamColumn(
                team = vm.team2,
                modifier = Modifier.weight(1f),
                vm = vm,
                isYellowActive = { vm.isYellowActive(it) },
                isRedActive = { vm.isRedActive(it) },
                yellowLabel = { player -> vm.formatClock(vm.yellowRemainingMs(player), false) },
                onPlayerTapped = { number ->
                    uiState = MatchScreenUiState.ActionMenu(2, number)
                }
            )
        }
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
                        Text("Select Action For ${selectedPlayer(state.teamIndex, state.playerNumber)}")

                        Button(
                            onClick = { uiState = MatchScreenUiState.ScorePick(state.teamIndex, state.playerNumber) }
                        ) {
                            Text("Score")
                        }

                        Button(
                            onClick = { uiState = MatchScreenUiState.SubPickOnPlayer(state.teamIndex, state.playerNumber) }
                        ) {
                            Text("Substitution")
                        }

                        Button(
                            onClick = { uiState = MatchScreenUiState.DiscPickType(state.teamIndex, state.playerNumber) }
                        ) {
                            Text("Discipline")
                        }
                    }
                }
            }
        }

        is MatchScreenUiState.ScorePick -> {
            ScoreDialogue(
                teamName = selectedTeamName(state.teamIndex),
                playerName = selectedPlayer(state.teamIndex, state.playerNumber),
                onConfirm = { scoreType ->
                    vm.recordScore(state.teamIndex, state.playerNumber, scoreType)
                    dismissDialogue()
                },
                onDismiss = dismissDialogue
            )
        }

        is MatchScreenUiState.SubPickOnPlayer -> {
            SubstitutePlayerDialogue(
                offNumber = state.offNumber,
                potentialSubs = selectedTeam(state.teamIndex).players.filter { !it.isOnField },
                onConfirm = { onNumber ->
                    uiState = MatchScreenUiState.SubPickReason(
                        teamIndex = state.teamIndex,
                        offNumber = state.offNumber,
                        onNumber = onNumber
                    )
                },
                onDismiss = dismissDialogue
            )
        }

        is MatchScreenUiState.SubPickReason -> {
            SubstituteReasonDialogue(
                onConfirm = { subType ->
                    vm.makeSub(state.teamIndex, state.offNumber, state.onNumber, subType)
                    dismissDialogue()
                },
                onDismiss = dismissDialogue
            )
        }

        is MatchScreenUiState.DiscPickType -> {
            DisciplineTypeDialogue(
                playerName = selectedPlayer(state.teamIndex, state.playerNumber),
                onConfirm = { discType ->
                    uiState = MatchScreenUiState.DiscPickReason(
                        teamIndex = state.teamIndex,
                        playerNumber = state.playerNumber,
                        type = discType
                    )
                },
                onDismiss = dismissDialogue
            )
        }

        is MatchScreenUiState.DiscPickReason -> {
            when (state.type) {
                DiscType.YELLOW -> {
                    DisciplineReasonYellowDialogue(
                        onConfirm = { reason ->
                            vm.recordDiscipline(state.teamIndex, state.playerNumber, state.type, reason)
                            dismissDialogue()
                        },
                        onDismiss = dismissDialogue
                    )
                }
                DiscType.RED -> {
                    DisciplineReasonRedDialogue(
                        onConfirm = { reason ->
                            vm.recordDiscipline(state.teamIndex, state.playerNumber, state.type, reason)
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
                        vm.resetSubs()
                        vm.resetDiscs()
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
        val canLogHalf = (vm.clock.elapsedMs >= 40L * 60L * 1000L) && (vm.halfTimeMs.longValue == 0L)
        AlertDialog(
            onDismissRequest = { showLogHalfDialog = false },
            title = { Text("Log Half") },
            text = {
                Text(if (canLogHalf) "Log Half?" else "It is not half time!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.logHalf()
                        showLogHalfDialog = false
                    },
                    enabled = canLogHalf
                ) {
                    Text("Yes, Log")
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

@Composable
fun ScoreDialogue(teamName: String,
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
fun SubstitutePlayerDialogue(
    offNumber: Int,
    potentialSubs: List<Player>,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: Player? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Substitution",
        prompt = "Substitute $offNumber for:",
        options = potentialSubs,
        selected = selected,
        optionLabel = { player -> "${player.number}. ${player.name.ifBlank { "(Unnamed)" }}" },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it.number) },
        onDismiss = onDismiss
    )
}

@Composable
fun SubstituteReasonDialogue(
    onConfirm: (SubType) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: SubType? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Substitution",
        prompt = "Reason for substitution:",
        options = SubType.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun DisciplineTypeDialogue(
    playerName: String,
    onConfirm: (DiscType) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: DiscType? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Discipline",
        prompt = "Card for $playerName:",
        options = DiscType.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun DisciplineReasonYellowDialogue(
    onConfirm: (DiscReason) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: DiscReason? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Discipline",
        prompt = "Discipline reason:",
        options = DiscReasonYellow.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun DisciplineReasonRedDialogue(
    onConfirm: (DiscReason) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: DiscReason? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Discipline",
        prompt = "Discipline reason:",
        options = DiscReasonRed.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
private fun playerTileColor(yellowActive: Boolean, redActive: Boolean) = when
{
    redActive -> Color(0xFFE74751)
    yellowActive -> Color(0xFFFFB834)
    else -> MaterialTheme.colorScheme.surface
}

@Composable
fun TeamColumn(team: Team, modifier: Modifier = Modifier, vm: MatchViewModel,
               isYellowActive: (Player) -> Boolean, isRedActive: (Player) -> Boolean,
               yellowLabel: (Player) -> String, onPlayerTapped: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(5.dp))
    {
        val onField = team.players
            .filter { it.isOnField }
            .sortedBy { it.fieldPos ?: 999 }

        LazyColumn {
            item{
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(team.name.ifBlank { "Team ${team.index}" }, textAlign = TextAlign.Center)
                }
            }
            items(onField.size) { i ->
                val player = onField[i]
                val locked = isYellowActive(player) || isRedActive(player) || !vm.isClockRunning()
                Surface(
                    color = playerTileColor(isYellowActive(player), isRedActive(player)),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .then(
                            if (!locked) Modifier.clickable { onPlayerTapped(player.number) }
                            else Modifier
                        )
                )
                {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                    {
                        Text("${player.number}. ${player.name}")

                        if (isYellowActive(player))
                        {
                            Text("Yellow: ${yellowLabel(player)}")
                        }
                        if (isRedActive(player))
                        {
                            Text("Red")
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun MatchScreenPreview() {
    MatchScreen(vm = MatchViewModel())
}