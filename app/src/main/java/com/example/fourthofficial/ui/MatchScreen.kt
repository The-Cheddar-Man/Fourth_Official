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
    var subTeamIndex by remember { mutableStateOf<Int?>(null) }
    var subOffNumber by remember { mutableStateOf<Int?>(null) }
    var subOnNumber by remember { mutableStateOf<Int?>(null) }

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
                    subTeamIndex = 1
                    subOffNumber = number
                    subOnNumber = null
                }
            )
            TeamColumn(
                team = vm.team2,
                modifier = Modifier.weight(1f),
                onPlayerTapped = { number ->
                    subTeamIndex = 2
                    subOffNumber = number
                    subOnNumber = null
                }
            )
        }
    }

    val teamIndex = subTeamIndex
    val offNumber = subOffNumber

    if (teamIndex != null && offNumber != null) {
        val team = if (teamIndex == 1) vm.team1 else vm.team2
        val bench = team.players.filter { !it.isOnField }

        // options should be sub/score/penalty

        AlertDialog(
            onDismissRequest = {
                subTeamIndex = null
                subOffNumber = null
                subOnNumber = null
            },
            title = { Text("Substitution") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sub off $offNumber for:")

                    bench.forEach { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { subOnNumber = player.number }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (subOnNumber == player.number),
                                onClick = { subOnNumber = player.number }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${player.number}. ${player.name.ifBlank { "(Unnamed)" }}")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = subOnNumber != null,
                    onClick = {
                        vm.makeSub(teamIndex, offNumber, subOnNumber!!)
                        subTeamIndex = null
                        subOffNumber = null
                        subOnNumber = null
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    subTeamIndex = null
                    subOffNumber = null
                    subOnNumber = null
                }) { Text("Cancel") }
            }
        )
    }
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