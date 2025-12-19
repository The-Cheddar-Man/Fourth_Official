package com.example.fourthofficial.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.model.Team
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

@Composable
fun MatchScreen(modifier: Modifier = Modifier,
                vm: MatchViewModel
) {
    var selectedNumber by remember { mutableStateOf<Int?>(null) }
    var selectedTeam by remember { mutableStateOf<Int?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        Text(vm.formatClock(vm.clock.elapsedMs), style = MaterialTheme.typography.displayMedium)
        Button(onClick = { vm.toggleClock() }) {
            Text(if (vm.clock.isRunning) "Stop clock" else "Start clock")
        }
        Row {
            TeamColumn(
                team = vm.team1,
                modifier = Modifier.weight(1f),
                onPlayerTapped = { number ->
                    selectedTeam = 1
                    selectedNumber = number
                }
            )
            TeamColumn(
                team = vm.team2,
                modifier = Modifier.weight(1f),
                onPlayerTapped = { number ->
                    selectedTeam = 2
                    selectedNumber = number
                }
            )
        }
    }

    val teamIndex = selectedTeam
    val offNumber = selectedNumber

    if (teamIndex != null && offNumber != null) {
        SubAlert(
            vm = vm,
            teamIndex = teamIndex,
            offNumber = offNumber,
            onConfirm = { onNumber ->
                vm.makeSub(teamIndex, offNumber, onNumber, "Unimplemented")
                selectedTeam = null
                selectedNumber = null
            },
            onDismiss = {
                selectedTeam = null
                selectedNumber = null
            }
        )
    }
}

@Composable
fun SubAlert(vm: MatchViewModel, teamIndex: Int, offNumber: Int,
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

@Preview(showBackground = true)
@Composable
private fun MatchScreenPreview() {
    MatchScreen(vm = MatchViewModel())
}