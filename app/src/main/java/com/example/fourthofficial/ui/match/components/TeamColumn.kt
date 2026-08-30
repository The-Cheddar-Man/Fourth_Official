package com.example.fourthofficial.ui.match.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.match.MatchPlayerState
import com.example.fourthofficial.domain.team.Team
import com.example.fourthofficial.ui.viewmodel.MatchViewModel


@Composable
private fun playerTileColor(yellowActive: Boolean, redActive: Boolean) = when {
    redActive -> Color(0xFFE74751)
    yellowActive -> Color(0xFFFFB834)
    else -> MaterialTheme.colorScheme.surface
}

@Composable
fun TeamColumn(
    team: Team, modifier: Modifier = Modifier.Companion, vm: MatchViewModel,
    playerStates: Map<PlayerId, MatchPlayerState>, onPlayerTapped: (PlayerId) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(5.dp)
    )
    {
        val onField = team.players.mapNotNull { player ->
            playerStates[player.id]?.let { state -> player to state }
        }.filter { (_, state) ->
            state.isOnField
        }.sortedBy { (_, state) ->
            state.fieldPos ?: 999
        }

        LazyColumn {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        team.name.ifBlank { "Team ${team.index}" },
                        textAlign = TextAlign.Center
                    )
                }
            }
            items(onField.size) { i ->
                val (player, state) = onField[i]
                val locked = !vm.canActOnPlayer(state)
                Surface(
                    color = playerTileColor(vm.isYellowActive(state), state.isRedCarded),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .then(
                            if (!locked) Modifier.clickable { onPlayerTapped(player.id) }
                            else Modifier.Companion
                        )
                )
                {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 6.dp
                        )
                    )
                    {
                        Text("${player.number}. ${player.name}")

                        if (vm.isYellowActive(state)) {
                            Text(
                                "Yellow: ${
                                    vm.formatClock(vm.yellowRemainingMs(state), false)
                                }"
                            )
                        }
                        if (state.isRedCarded) {
                            Text("Red")
                        }
                    }
                }
            }
        }
    }
}