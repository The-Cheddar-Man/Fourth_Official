package com.example.fourthofficial.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.model.Discipline
import com.example.fourthofficial.model.Score
import com.example.fourthofficial.model.Substitution
import com.example.fourthofficial.domain.team.Team
import com.example.fourthofficial.ui.components.DataTable
import com.example.fourthofficial.ui.components.TableColumn
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

enum class SummaryTab {
    Scores, Substitutions, Disciplines, Export
}

@Composable
fun SummaryScreen(modifier: Modifier = Modifier, vm: MatchViewModel) {
    var currentTab by rememberSaveable { mutableStateOf(SummaryTab.Scores) }
    var selectedTeam by rememberSaveable { mutableIntStateOf(1) }
    var selectedHalf by rememberSaveable { mutableIntStateOf(1) }
    val team = if (selectedTeam == 1) vm.team1 else vm.team2

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(
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
        if (currentTab != SummaryTab.Export) {
            SummaryFilters(
                team = team,
                halfIndex = selectedHalf,
                onSwitchTeam = {
                    selectedTeam = switchTeams(selectedTeam)
                },
                onSwitchHalf = {
                    selectedHalf = switchHalf(selectedHalf)
                }
            )
        }

        when (currentTab) {
            SummaryTab.Scores -> ScoresTab(vm = vm, team = team, halfIndex = selectedHalf)
            SummaryTab.Substitutions -> SubstitutionsTab(vm = vm, team = team, halfIndex = selectedHalf)
            SummaryTab.Disciplines -> DisciplinesTab(vm = vm, team = team, halfIndex = selectedHalf)
            SummaryTab.Export -> ExportTab(vm = vm)
        }
    }
}

@Composable
private fun ScoresTab(modifier: Modifier = Modifier, vm: MatchViewModel, team: Team, halfIndex: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Match Scores",
            style = MaterialTheme.typography.headlineMedium
        )

        val events = vm.scoreEvents
            .filter { it.teamId == team.id && it.halfIndex == halfIndex }
            .sortedBy { it.timeMs }

        val columns = listOf(
            TableColumn(header = "Type", weight = 1.5f) { e ->
                e.type.label
            },
            TableColumn(header = "Player", weight = 1.5f) { e ->
                playerLabel(team, e.playerId )
            },
            TableColumn<Score>(header = "Time", weight = 0.8f) { e ->
                vm.formatClock(e.timeMs, false)
            }
        )

        DataTable(events = events, columns = columns, Modifier.fillMaxWidth().weight(1f), keySelector = { it.id.value })
    }
}

@Composable
private fun SubstitutionsTab(modifier: Modifier = Modifier, vm: MatchViewModel, team: Team, halfIndex: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Match Substitutions", style = MaterialTheme.typography.headlineMedium)

        val events = vm.subEvents
            .filter { it.teamId == team.id && it.halfIndex == halfIndex}
            .sortedBy { it.timeMs }

        val columns = listOf(
            TableColumn(header = "Off", weight = 1.5f) { e ->
                playerLabel(team, e.playerOffId)
            },
            TableColumn(header = "Reason", weight = 1.5f) { e ->
                e.type.label
            },
            TableColumn(header = "On", weight = 1.5f) { e ->
                playerLabel(team, e.playerOnId)
            },
            TableColumn<Substitution>(header = "Time", weight = 0.8f) { e ->
                vm.formatClock(e.timeMs, false)
            }
        )

        DataTable(events = events, columns = columns, Modifier.fillMaxWidth().weight(1f), keySelector = { it.id.value })
    }
}

@Composable
private fun DisciplinesTab(modifier: Modifier = Modifier, vm: MatchViewModel, team: Team, halfIndex: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Match Disciplines", style = MaterialTheme.typography.headlineMedium)

        val events = vm.discEvents
            .filter { it.teamId == team.id && it.halfIndex == halfIndex}
            .sortedBy { it.timeMs }

        val columns = listOf(
            TableColumn(header = "Type", weight = 1.5f) { e ->
                e.type.label
            },
            TableColumn(header = "Reason", weight = 1.5f) { e ->
                e.reason.label
            },
            TableColumn(header = "Player", weight = 1.5f) { e ->
                playerLabel(team, e.playerId)
            },
            TableColumn<Discipline>(header = "Time", weight = 0.8f) { e ->
                vm.formatClock(e.timeMs, false)
            }
        )

        DataTable(events = events, columns = columns, Modifier.fillMaxWidth().weight(1f), keySelector = { it.id.value })
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
        Text("Export Data (TBC)", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun SummaryFilters(
    team: Team,
    halfIndex: Int,
    onSwitchTeam: () -> Unit,
    onSwitchHalf: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onSwitchTeam) {
                Text(
                    team.name.ifBlank {
                        "Team ${team.index}"
                    }
                )
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onSwitchHalf) {
                Text("Half $halfIndex")
            }
        }
    }
}

private fun switchTeams(index: Int) =
    if (index == 1) 2 else 1

private fun switchHalf(index: Int) =
    if (index == 1) 2 else 1

private fun playerLabel(team: Team, playerId: PlayerId): String {
    return team.players
        .find { it.id == playerId }
        ?.let { "${it.number}. ${it.name.ifBlank { "(Unnamed)" }}" }
        ?: "Unknown player"
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreview() {
    SummaryScreen(vm = MatchViewModel())
}