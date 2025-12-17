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

data class Team(
    val name: String,
    val players: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(modifier: Modifier = Modifier) {
    var team1 by remember { mutableStateOf(Team("Team 1", List(23) { "Player ${it+1}" })) }
    var team2 by remember { mutableStateOf(Team("Team 2", List(23) { "Player ${it+1}" })) }

    Row(
        modifier = modifier
            .fillMaxSize()
    ) {
        TeamColumn(
            team = team1,
            modifier = Modifier.weight(1f)
        )
        TeamColumn(
            team = team2,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TeamColumn(team: Team, modifier: Modifier = Modifier) {
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
            items(team.players.size) { i -> Text(team.players[i]) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Text("Edit Team")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SetupScreenPreview() {
    SetupScreen()
}