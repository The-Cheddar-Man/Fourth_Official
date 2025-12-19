package com.example.fourthofficial.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.model.Discipline
import com.example.fourthofficial.model.Score
import com.example.fourthofficial.model.Substitution
import com.example.fourthofficial.ui.components.DataTable
import com.example.fourthofficial.ui.components.TableColumn
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

enum class SummaryTab {
    Scores, Substitutions, Disciplines, Export
}

@Composable
fun SummaryScreen(modifier: Modifier = Modifier, vm: MatchViewModel) {
    var currentTab by rememberSaveable { mutableStateOf(SummaryTab.Scores) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = currentTab.ordinal
        ) {
            SummaryTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = currentTab.ordinal == index,
                    onClick = { currentTab = tab },
                    text = {
                        Text(
                            tab.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        when (currentTab) {
            SummaryTab.Scores -> ScoresTab(vm = vm)
            SummaryTab.Substitutions -> SubstitutionsTab(vm = vm)
            SummaryTab.Disciplines -> DisciplinesTab(vm = vm)
            SummaryTab.Export -> ExportTab(vm = vm)
        }
    }
}

@Composable
private fun ScoresTab(modifier: Modifier = Modifier, vm: MatchViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var teamIndex by rememberSaveable { mutableStateOf(1) }
        Text("Match Scores", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = {teamIndex = switchTeams(teamIndex)}) { Text(if (teamIndex == 1) vm.team1.name else vm.team2.name) }

        val team = if(teamIndex == 1) vm.team1 else vm.team2

        val events = vm.scoreEvents
            .filter { it.teamIndex == teamIndex }
            .sortedBy { it.timeMs }

        val columns = listOf(
            TableColumn<Score>(header = "Type", weight = 1.5f) { e ->
                e.type
            },
            TableColumn<Score>(header = "Player", weight = 1.5f) { e ->
                "${e.player}. ${team.players[e.player-1].name}"
            },
            TableColumn<Score>(header = "Time", weight = 0.8f) { e ->
                vm.formatClock(e.timeMs)
            }
        )

        DataTable<Score>(events = events, columns = columns, Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun SubstitutionsTab(modifier: Modifier = Modifier, vm: MatchViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var teamIndex by rememberSaveable { mutableStateOf(1) }
        Text("Match Substitutions", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = {teamIndex = switchTeams(teamIndex)}) { Text(if (teamIndex == 1) vm.team1.name else vm.team2.name) }

        val team = if(teamIndex == 1) vm.team1 else vm.team2

        val events = vm.subEvents
            .filter { it.teamIndex == teamIndex }
            .sortedBy { it.timeMs }

        val columns = listOf(
            TableColumn<Substitution>(header = "Off", weight = 1.5f) { e ->
                "${e.playerOff}. ${team.players[e.playerOff-1].name}"
            },
            TableColumn<Substitution>(header = "Reason", weight = 1.5f) { e ->
                e.reason
            },
            TableColumn<Substitution>(header = "On", weight = 1.5f) { e ->
                "${e.playerOn}. ${team.players[e.playerOn-1].name}"
            },
            TableColumn<Substitution>(header = "Time", weight = 0.8f) { e ->
                vm.formatClock(e.timeMs)
            }
        )

        DataTable<Substitution>(events = events, columns = columns, Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun DisciplinesTab(modifier: Modifier = Modifier, vm: MatchViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var teamIndex by rememberSaveable { mutableStateOf(1) }
        Text("Match Disciplines", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = {teamIndex = switchTeams(teamIndex)}) { Text(if (teamIndex == 1) vm.team1.name else vm.team2.name) }

        val team = if(teamIndex == 1) vm.team1 else vm.team2

        val events = vm.discEvents
            .filter { it.teamIndex == teamIndex }
            .sortedBy { it.timeMs }

        val columns = listOf(
            TableColumn<Discipline>(header = "Type", weight = 1.5f) { e ->
                e.type
            },
            TableColumn<Discipline>(header = "Reason", weight = 1.5f) { e ->
                e.reason
            },
            TableColumn<Discipline>(header = "Player", weight = 1.5f) { e ->
                "${e.player}. ${team.players[e.player-1].name}"
            },
            TableColumn<Discipline>(header = "Time", weight = 0.8f) { e ->
                vm.formatClock(e.timeMs)
            }
        )

        DataTable<Discipline>(events = events, columns = columns, Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun ExportTab(modifier: Modifier = Modifier, vm: MatchViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var teamIndex by rememberSaveable { mutableStateOf(1) }
        Text("Export Data", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = {teamIndex = switchTeams(teamIndex)}) { Text(if (teamIndex == 1) vm.team1.name else vm.team2.name) }
    }
}

private fun switchTeams(teamIndex: Int) : Int
{
    if(teamIndex == 1)
        return 2
    return 1
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreview() {
    SummaryScreen(vm = MatchViewModel())
}