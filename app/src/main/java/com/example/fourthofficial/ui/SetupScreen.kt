package com.example.fourthofficial.ui

import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import com.example.fourthofficial.model.Team

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(modifier: Modifier = Modifier,
                team1 : Team, team2 : Team,
                onTeam1Change: (Team) -> Unit,
                onTeam2Change: (Team) -> Unit
) {
    var editingSide by remember { mutableStateOf<Int?>(null) }

    if (editingSide != null) {
        val current = if (editingSide == 1) team1 else team2

        ModalBottomSheet(
            onDismissRequest = { editingSide = null }
        ) {
            EditTeamSheet(
                team = current,
                onSave = { updated ->
                    if (editingSide == 1) onTeam1Change(updated) else onTeam2Change(updated)
                    editingSide = null
                },
                onCancel = { editingSide = null }
            )
        }
    }

    Row(modifier = modifier.fillMaxSize()) {
        TeamColumn(
            team = team1,
            onEdit = { editingSide = 1 },
            modifier = Modifier.weight(1f)
        )
        TeamColumn(
            team = team2,
            onEdit = { editingSide = 2 },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TeamColumn(team: Team, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxHeight()
            .padding(5.dp))
    {
        Text(team.name, style = MaterialTheme.typography.headlineMedium)
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(team.players.size) { i -> Text((i+1).toString() + ". " + team.players[i]) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
            Text("Edit Team")
        }
    }
}

@Composable
fun EditTeamSheet(team: Team, onSave: (Team) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf(team.name) }
    val players = remember(team) { mutableStateListOf(*team.players.toTypedArray()) }

    Column(
        Modifier
        .padding(16.dp)
    ) {
        Text("Team Name", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Team name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Players", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(
                count = 23,
                key = {index -> index}
            ) {
                i -> OutlinedTextField(
                    value = players[i],
                    onValueChange = { players[i] = it },
                    label = { Text("Player ${i + 1}") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(
                onClick = { onSave(Team(name, players.toList())) },
                modifier = Modifier.weight(1f)
            ) { Text("Save") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SetupScreenPreview() {
    val team1 = Team("Team 1", List(23) { "" })
    val team2 = Team("Team 2", List(23) { "" })

    SetupScreen(
        team1 = team1,
        team2 = team2,
        onTeam1Change = {},
        onTeam2Change = {}
    )
}