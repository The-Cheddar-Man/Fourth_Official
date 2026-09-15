package com.example.fourthofficial.ui.match.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchPhase
import com.example.fourthofficial.ui.theme.AppButtonShape
import com.example.fourthofficial.ui.theme.AppCardShape
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
    onFinishHalfRequested: () -> Unit,
    bodyContent: @Composable () -> Unit
) {
    var clockDisplayMode by remember { mutableStateOf(ClockDisplayMode.RUGBY) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    )
    {
        MatchHeader(
            vm = vm,
            clockDisplayMode = clockDisplayMode,
            onClockTapped = {
                clockDisplayMode = when (clockDisplayMode) {
                    ClockDisplayMode.RUGBY -> ClockDisplayMode.HALF
                    ClockDisplayMode.HALF -> ClockDisplayMode.TOTAL
                    ClockDisplayMode.TOTAL -> ClockDisplayMode.RUGBY
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { vm.toggleClock() },
                enabled = vm.phase != MatchPhase.FINISHED,
                shape = AppButtonShape,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (vm.clock.isRunning) { "Stop Clock" } else { "Start Clock" })
            }

            OutlinedButton(
                onClick = onFinishHalfRequested,
                enabled = vm.phase == MatchPhase.FIRST_HALF || vm.phase == MatchPhase.SECOND_HALF,
                shape = AppButtonShape,
                modifier = Modifier.weight(1f)
            ) {
                Text(when (vm.phase) {
                        MatchPhase.SECOND_HALF -> "End Match"
                        MatchPhase.FINISHED -> "Match Finished"
                        else -> "Log Half"
                    }
                )
            }
        }

        bodyContent()
    }
}

@Composable
private fun MatchHeader(vm: MatchViewModel, clockDisplayMode: ClockDisplayMode, onClockTapped: () -> Unit)
{
    val elapsedToDisplay =
        when (clockDisplayMode) {
            ClockDisplayMode.RUGBY -> vm.displayElapsedMs
            ClockDisplayMode.HALF -> vm.clock.halfElapsedMs
            ClockDisplayMode.TOTAL -> vm.totalDisplayElapsedMs
        }

    val clockLabel =
        when (clockDisplayMode) {
            ClockDisplayMode.RUGBY -> "MATCH TIME"
            ClockDisplayMode.HALF -> "HALF ELAPSED"
            ClockDisplayMode.TOTAL -> "TOTAL ELAPSED"
        }

    val showHalfTimeScore =
        vm.phase == MatchPhase.HALF_TIME || vm.phase == MatchPhase.SECOND_HALF || vm.phase == MatchPhase.FINISHED

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppCardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = phaseLabel(vm.phase),
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 4.dp
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color =
                        MaterialTheme.colorScheme
                            .onPrimaryContainer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                TeamScoreBlock(
                    modifier = Modifier.weight(1f),
                    teamName =
                        vm.team1.name.ifBlank {
                            "Team ${vm.team1.index}"
                        },
                    score =
                        vm.scoreForTeam(vm.team1.id),
                    halfTimeScore =
                        if (showHalfTimeScore) {
                            vm.halfTimeScoreForTeam(
                                vm.team1.id
                            )
                        } else {
                            null
                        }
                )

                ClockBlock(
                    modifier = Modifier.weight(1.25f),
                    elapsedMs = elapsedToDisplay,
                    remainingMs = vm.halfRemainingMs,
                    clockLabel = clockLabel,
                    vm = vm,
                    onClick = onClockTapped
                )

                TeamScoreBlock(
                    modifier = Modifier.weight(1f),
                    teamName =
                        vm.team2.name.ifBlank {
                            "Team ${vm.team2.index}"
                        },
                    score =
                        vm.scoreForTeam(vm.team2.id),
                    halfTimeScore =
                        if (showHalfTimeScore) {
                            vm.halfTimeScoreForTeam(
                                vm.team2.id
                            )
                        } else {
                            null
                        }
                )
            }
        }
    }
}
@Composable
private fun TeamScoreBlock(modifier: Modifier = Modifier, teamName: String,
    score: Int, halfTimeScore: Int?)
{
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = teamName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )

        if (halfTimeScore != null) {
            Text(
                text = "HT $halfTimeScore",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ClockBlock(modifier: Modifier = Modifier, elapsedMs: Long, remainingMs: Long,
                       clockLabel: String, vm: MatchViewModel, onClick: () -> Unit)
{
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = vm.formatClock(elapsedMs, false),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = clockLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "${vm.formatClock(remainingMs, true)} REMAINING",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
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

private fun phaseLabel(phase: MatchPhase): String =
    when (phase) {
        MatchPhase.NOT_STARTED -> "NOT STARTED"
        MatchPhase.FIRST_HALF -> "FIRST HALF"
        MatchPhase.HALF_TIME -> "HALF TIME"
        MatchPhase.SECOND_HALF -> "SECOND HALF"
        MatchPhase.FINISHED -> "FULL TIME"
    }