package com.example.fourthofficial.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.model.Team
import com.example.fourthofficial.ui.viewmodel.MatchClockViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MatchScreen(modifier: Modifier = Modifier,
                team1 : Team, team2 : Team, clockVm: MatchClockViewModel = viewModel()
) {
    val clock by clockVm.state.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(formatClock(clock.elapsedMs), style = MaterialTheme.typography.displayMedium)
        Button(onClick = { clockVm.toggle() }) {
            Text(if (clock.isRunning) "Stop clock" else "Start clock")
        }
        Row(modifier = modifier.fillMaxSize()) {
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
            items(15) { i -> Text((i+1).toString() + ". " + team.players[i]) }
        }
    }
}

@Composable
private fun formatClock(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true)
@Composable
private fun MatchScreenPreview() {
    val team1 = Team("Team 1", List(23) { "" })
    val team2 = Team("Team 2", List(23) { "" })

    MatchScreen(
        team1 = team1,
        team2 = team2
    )
}