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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.model.Team
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(modifier: Modifier = Modifier,
                vm: MatchViewModel,
                onTeam1Change: (Team) -> Unit,
                onTeam2Change: (Team) -> Unit
) {
    var editingSide by remember { mutableStateOf<Int?>(null) }
    val clearSelection = { editingSide = null }

    if (editingSide != null) {
        val current = if (editingSide == 1) vm.team1 else vm.team2

        ModalBottomSheet(
            onDismissRequest = { clearSelection() }
        ) {
            EditTeamSheet(
                team = current,
                onSave = { updated ->
                    if (editingSide == 1) onTeam1Change(updated) else onTeam2Change(updated)
                    clearSelection()
                },
                onCancel = { clearSelection() }
            )
        }
    }

    Row(modifier = modifier.fillMaxSize()) {
        TeamColumn(
            team = vm.team1,
            onEdit = { editingSide = 1 },
            modifier = Modifier.weight(1f)
        )
        TeamColumn(
            team = vm.team2,
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
            items(team.players.size) { i -> Text((i+1).toString() + ". " + team.players[i].name) }
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
    val index = team.index
    val players = remember(team) { mutableStateListOf(*team.players.toTypedArray()) }

    Column(
        Modifier.padding(16.dp)
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
                value = players[i].name,
                onValueChange = { newName -> players[i] = players[i].copy(name = newName) },
                    label = { Text("Player ${i + 1}") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(
                onClick = { onSave(Team(name, index, players.toList())) },
                modifier = Modifier.weight(1f)
            ) { Text("Save") }
        }
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