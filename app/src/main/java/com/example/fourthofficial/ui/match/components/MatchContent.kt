package com.example.fourthofficial.ui.match.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchPhase
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

private enum class ClockDisplayMode {
    RUGBY,
    HALF,
    TOTAL
}

@Composable
fun MatchContent(
    modifier: Modifier = Modifier,
    vm: MatchViewModel,
    onStartNewMatchRequested: () -> Unit,
    onFinishHalfRequested: () -> Unit,
    bodyContent: @Composable () -> Unit
) {
    var clockDisplayMode by remember { mutableStateOf(ClockDisplayMode.RUGBY) }

    val elapsedToDisplay = when (clockDisplayMode) {
        ClockDisplayMode.RUGBY -> vm.displayElapsedMs
        ClockDisplayMode.HALF -> vm.clock.halfElapsedMs
        ClockDisplayMode.TOTAL -> vm.totalDisplayElapsedMs
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    )
    {
        //region Timers
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                when (clockDisplayMode) {
                    ClockDisplayMode.RUGBY -> "Elapsed Time"
                    ClockDisplayMode.HALF -> "Half Elapsed Time"
                    ClockDisplayMode.TOTAL -> "Total Elapsed Time"
                },
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "Remaining Time",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                vm.formatClock(elapsedToDisplay, false),
                modifier = Modifier.weight(1f).clickable {
                    clockDisplayMode = when (clockDisplayMode) {
                        ClockDisplayMode.RUGBY -> ClockDisplayMode.HALF
                        ClockDisplayMode.HALF -> ClockDisplayMode.TOTAL
                        ClockDisplayMode.TOTAL -> ClockDisplayMode.RUGBY
                    }
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                vm.formatClock(vm.halfRemainingMs, true),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayMedium
            )
        }
        //endregion

        //region Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(onClick = { vm.toggleClock() }, modifier = Modifier.weight(1f)) {
                Text(if (vm.clock.isRunning) "Stop clock" else "Start clock")
            }
            Button(
                onClick = onStartNewMatchRequested,
                modifier = Modifier.weight(1f)
            ) {
                Text("Start New Match", textAlign = TextAlign.Center)
            }
            Button(
                onClick = onFinishHalfRequested,
                enabled = vm.phase != MatchPhase.FINISHED,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    when (vm.phase) {
                        MatchPhase.SECOND_HALF -> "End Match"
                        MatchPhase.FINISHED -> "Match Finished"
                        else -> "Log Half"
                    }
                )
            }
        }
        //endregion

        //region Scores
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.scoreForTeam(vm.team1.id).toString()
            )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = "Score"
            )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.scoreForTeam(vm.team2.id).toString()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.halfTimeScoreForTeam(vm.team1.id).toString()
            )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = "Score (HT)"
            )
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                text = vm.halfTimeScoreForTeam(vm.team2.id).toString()
            )
        }
        //endregion

        bodyContent()
    }
}

@Composable
fun MatchTeamColumns(
    vm: MatchViewModel,
    onPlayerTapped: (TeamId, PlayerId) -> Unit,
    onPlayerLongPressed: (TeamId, PlayerId) -> Unit,
    onPreparedSubstitutionsTapped: (TeamId) -> Unit
) {
    Row {
        TeamColumn(
            team = vm.team1,
            modifier = Modifier.weight(1f),
            vm = vm,
            playerStates = vm.team1PlayerStates,
            onPlayerTapped = { playerId -> onPlayerTapped(vm.team1.id, playerId) },
            onPlayerLongPressed = { playerId -> onPlayerLongPressed(vm.team1.id, playerId) },
            onPreparedSubstitutionsTapped = { onPreparedSubstitutionsTapped(vm.team1.id) }
        )
        TeamColumn(
            team = vm.team2,
            modifier = Modifier.weight(1f),
            vm = vm,
            playerStates = vm.team2PlayerStates,
            onPlayerTapped = { playerId -> onPlayerTapped(vm.team2.id, playerId) },
            onPlayerLongPressed = { playerId -> onPlayerLongPressed(vm.team2.id, playerId) },
            onPreparedSubstitutionsTapped = { onPreparedSubstitutionsTapped(vm.team2.id) }
        )
    }
}