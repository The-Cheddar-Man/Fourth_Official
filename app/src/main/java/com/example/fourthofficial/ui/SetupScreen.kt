package com.example.fourthofficial.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.model.Player
import com.example.fourthofficial.model.Team
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

@Composable
fun SetupScreen(modifier: Modifier = Modifier, vm: MatchViewModel,
                onTeam1Change: (Team) -> Unit, onTeam2Change: (Team) -> Unit) {
    Row(modifier = modifier.fillMaxSize()) {

        TeamColumn(
            team = vm.team1,
            onSave = onTeam1Change,
            modifier = Modifier.weight(1f)
        )

        TeamColumn(
            team = vm.team2,
            onSave = onTeam2Change,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TeamColumn(team: Team, onSave: (Team) -> Unit, modifier: Modifier = Modifier) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember(team) { mutableStateOf(team.name) }
    val editedPlayers = remember(team) {
        mutableStateListOf(*team.players.toTypedArray())
    }

    LaunchedEffect(team, isEditing) {
        if (!isEditing) {
            editedName = team.name
            editedPlayers.clear()
            editedPlayers.addAll(team.players)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        if (!isEditing) {
            Text(
                text = team.name.ifBlank { "Team ${team.index}" },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(team.players.size) { i ->
                    Text("${team.players[i].number}. ${team.players[i].name}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Team")
            }
        } else {
            Text(
                text = "Edit Team ${team.index}",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Team Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    count = editedPlayers.size,
                    key = { idx -> idx }
                ) { i ->
                    EditPlayerRow(
                        index = i,
                        player = editedPlayers[i],
                        onPlayerChange = { updated ->
                            editedPlayers[i] = updated
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        editedName = team.name
                        editedPlayers.clear()
                        editedPlayers.addAll(team.players)
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        onSave(team.copy(name = editedName, players = editedPlayers.toList()))
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save") }
            }
        }
    }
}

@Composable
private fun EditPlayerRow(index: Int, player: Player, onPlayerChange: (Player) -> Unit) {
    var playerNum by remember(player.number) { mutableStateOf(player.number.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = playerNum,
            onValueChange = { newText ->
                val filtered = newText.filter { it.isDigit() }
                playerNum = filtered

                val n = filtered.toIntOrNull()
                if (n != null) {
                    onPlayerChange(player.copy(number = n))
                }
            },
            label = { Text("#") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(0.5f)
        )

        OutlinedTextField(
            value = player.name,
            onValueChange = { newName ->
                onPlayerChange(player.copy(name = newName))
            },
            label = { Text("Player ${index + 1}") },
            singleLine = true,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun SetupScreenPreview() {
    SetupScreen(
        vm = MatchViewModel(),
        onTeam1Change = {},
        onTeam2Change = {}
    )
}
