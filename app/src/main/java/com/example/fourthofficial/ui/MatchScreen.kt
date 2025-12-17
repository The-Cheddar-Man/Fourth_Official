package com.example.fourthofficial.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        Text(formatClock(vm.clock.elapsedMs), style = MaterialTheme.typography.displayMedium)
        Button(onClick = { vm.toggleClock() }) {
            Text(if (vm.clock.isRunning) "Stop clock" else "Start clock")
        }
        Row {
            TeamColumn(
                team = vm.team1,
                modifier = Modifier.weight(1f)
            )
            TeamColumn(
                team = vm.team2,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TeamColumn(team: Team, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(5.dp))
    {
        Text(team.name, style = MaterialTheme.typography.headlineMedium)
        LazyColumn {
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
    val vm = MatchViewModel()

    MatchScreen(vm = vm)
}