package com.example.fourthofficial.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fourthofficial.model.Team
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

enum class DialogStep {
    MAIN,
    SCORE,
    SUB,
    DISC,
    NONE
}

@Composable
fun MatchScreen(modifier: Modifier = Modifier,
                vm: MatchViewModel
) {
    var selectedNumber by remember { mutableStateOf<Int?>(null) }
    var selectedTeam by remember { mutableStateOf<Int?>(null) }
    var dialogStep by remember { mutableStateOf(DialogStep.NONE) }

    val clearSelection = {
        selectedTeam = null
        selectedNumber = null
        dialogStep = DialogStep.NONE
    }

    fun setDialogState(state:DialogStep) {dialogStep = state}

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
            Button(onClick = { vm.resetClock() }, modifier = Modifier.weight(1f)) {
                Text( "Reset Clock")
            }
            Button(onClick = { vm.logHalf() }, modifier = Modifier.weight(1f)) {
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
                text = "0" )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = "Score" )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = "0" )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = "0" )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = "Score (HT)" )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = "0" )
        }

        Row {
            TeamColumn(
                team = vm.team1,
                modifier = Modifier.weight(1f),
                onPlayerTapped = { number ->
                    selectedTeam = 1
                    selectedNumber = number
                    dialogStep = DialogStep.MAIN
                }
            )
            TeamColumn(
                team = vm.team2,
                modifier = Modifier.weight(1f),
                onPlayerTapped = { number ->
                    selectedTeam = 2
                    selectedNumber = number
                    dialogStep = DialogStep.MAIN
                }
            )
        }
    }

    val teamIndex = selectedTeam
    val offNumber = selectedNumber

    if (teamIndex != null && offNumber != null) {
        val team = if (teamIndex == 1) vm.team1 else vm.team2
        when (dialogStep) {

            DialogStep.MAIN -> {
                Dialog(onDismissRequest = { setDialogState(DialogStep.NONE) })
                {
                    Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .padding(24.dp),
                )
                {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("Select Action For ${team.players[offNumber-1].name}")
                        Button(onClick = { setDialogState(DialogStep.SCORE) }) {
                            Text("Score")
                        }
                        Button(onClick = { setDialogState(DialogStep.SUB) }) {
                            Text("Substitution")
                        }
                        Button(onClick = { setDialogState(DialogStep.DISC) }) {
                            Text("Discipline")
                        }
                    }
                }
            }
                }

            DialogStep.SCORE -> {
                ScoreDialogue(
                    vm = vm,
                    teamIndex = teamIndex,
                    playerNumber = offNumber,
                    onConfirm = { scoreType ->
                        vm.recordScore(teamIndex, offNumber, scoreType)
                        clearSelection()
                    },
                    onDismiss = {
                        clearSelection()
                    }
                )
            }

            DialogStep.SUB -> {
                SubDialogue(
                    vm = vm,
                    teamIndex = teamIndex,
                    offNumber = offNumber,
                    onConfirm = { onNumber ->
                        vm.makeSub(teamIndex, offNumber, onNumber, "Unimplemented")
                        clearSelection()
                    },
                    onDismiss = {
                        clearSelection()
                    }
                )
            }

            DialogStep.DISC -> {
                DisciplineDialogue(
                    vm = vm,
                    teamIndex = teamIndex,
                    playerNumber = offNumber,
                    onConfirm = { discType ->
                        vm.recordDiscipline( teamIndex, offNumber, discType, "Unimplemented")
                        clearSelection()
                    },
                    onDismiss = {
                        clearSelection()
                    }
                )
            }

            DialogStep.NONE -> Unit
        }
    }
}

@Composable
fun ScoreDialogue(vm: MatchViewModel, teamIndex: Int, playerNumber: Int,
             onConfirm: (String) -> Unit, onDismiss: () -> Unit
) {
    val scoreTypes = listOf("Try", "Conversion", "Penalty Kick", "Drop Goal")
    val team = if (teamIndex == 1) vm.team1 else vm.team2
    var chosenType by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scoring") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Player ${team.players[playerNumber - 1].name} Scored:")

                scoreTypes.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { chosenType = type }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (chosenType == type),
                            onClick = { chosenType = type }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(type)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = chosenType != null,
                onClick = { onConfirm(chosenType!!) }
            ) { Text("Confirm") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SubDialogue(vm: MatchViewModel, teamIndex: Int, offNumber: Int,
             onConfirm: (Int) -> Unit, onDismiss: () -> Unit
) {
    var onNumber by remember { mutableStateOf<Int?>(null) }
    val team = if (teamIndex == 1) vm.team1 else vm.team2
    val bench = team.players.filter { !it.isOnField }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Substitution") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sub off $offNumber for:")

                bench.forEach { player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNumber = player.number }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (onNumber == player.number),
                            onClick = { onNumber = player.number }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("${player.number}. ${player.name.ifBlank { "(Unnamed)" }}")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = onNumber != null,
                onClick = { onConfirm(onNumber!!) }
            ) { Text("Confirm") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DisciplineDialogue(vm: MatchViewModel, teamIndex: Int, playerNumber: Int,
                  onConfirm: (String) -> Unit, onDismiss: () -> Unit
) {
    val discTypes = listOf("Yellow Card", "Red Card")
    val team = if (teamIndex == 1) vm.team1 else vm.team2
    var chosenType by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scoring") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Player ${team.players[playerNumber - 1].name} Disciplined:")

                discTypes.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { chosenType = type }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (chosenType == type),
                            onClick = { chosenType = type }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(type)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = chosenType != null,
                onClick = { onConfirm(chosenType!!) }
            ) { Text("Confirm") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TeamColumn(team: Team, modifier: Modifier = Modifier, onPlayerTapped: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(5.dp))
    {
        val onField = team.players
            .filter { it.isOnField }
            .sortedBy { it.fieldPos ?: 999 }

        LazyColumn {
            items(onField.size) { i ->
                val player = onField[i]
                Text(
                    text = "${player.number}. ${player.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayerTapped(player.number) }
                        .padding(4.dp)
                )
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