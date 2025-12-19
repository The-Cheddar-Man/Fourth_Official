package com.example.fourthofficial.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

enum class SummaryTab {
    Scores, Substitutions, Disciplines, Export
}

@Composable
fun SummaryScreen(modifier: Modifier = Modifier, vm: MatchViewModel) {
    var currentTab by rememberSaveable { mutableStateOf(SummaryTab.Substitutions) }

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
            SummaryTab.Scores -> ScoresTab()
            SummaryTab.Substitutions -> SubstitutionsTab(vm = vm)
            SummaryTab.Disciplines -> DisciplinesTab()
            SummaryTab.Export -> ExportTab()
        }
    }
}

@Composable
private fun ScoresTab(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Match Scores", style = MaterialTheme.typography.headlineMedium)
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
        Text("Match Substitutions", style = MaterialTheme.typography.headlineMedium)

        var teamIndex by rememberSaveable { mutableStateOf(1) }
        Button(onClick = {teamIndex = switchTeams(teamIndex)}) { Text(if (teamIndex == 1) vm.team1.name else vm.team2.name) }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            item {
                Row(Modifier.fillMaxWidth()) {
                    Text("Off", modifier = Modifier.weight(1.5f))
                    Text("Reason", modifier = Modifier.weight(1.5f))
                    Text("On", modifier = Modifier.weight(1.5f))
                    Text("Time", modifier = Modifier.weight(0.8f))
                }
                HorizontalDivider()
            }
            val team = if(teamIndex == 1) vm.team1 else vm.team2
            items(
                items = vm.subEvents.sortedBy { it.timeMs }.filter { it.teamIndex == team.index },
                key = { e -> "${e.timeMs}-${e.teamIndex}-${e.playerOff}-${e.playerOn}" }
            ) { e ->
                val offName = team.players.firstOrNull { it.number == e.playerOff }?.name.orEmpty()
                val onName  = team.players.firstOrNull { it.number == e.playerOn }?.name.orEmpty()

                Row(Modifier.fillMaxWidth()) {
                    Text("${e.playerOff}. ${offName.ifBlank { "(Unnamed)" }}",
                        modifier = Modifier.weight(1.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text("unimplemented",
                        modifier = Modifier.weight(1.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text("${e.playerOn}. ${onName.ifBlank { "(Unnamed)" }}",
                        modifier = Modifier.weight(1.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text(vm.formatClock(e.timeMs), modifier =
                        Modifier.weight(0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
                HorizontalDivider()
            }
        }
    }
}

fun switchTeams(teamIndex: Int) : Int
{
    if(teamIndex == 1)
        return 2
    return 1
}

@Composable
private fun DisciplinesTab(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Match Disciplines", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun ExportTab(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Export Data", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreview() {
    SummaryScreen(vm = MatchViewModel())
}