package com.example.fourthofficial.ui.match.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.match.MatchPlayerState
import com.example.fourthofficial.domain.team.Team
import com.example.fourthofficial.ui.common.PlayerTile
import com.example.fourthofficial.ui.common.TeamPanel
import com.example.fourthofficial.ui.theme.OnRedCard
import com.example.fourthofficial.ui.theme.OnYellowCard
import com.example.fourthofficial.ui.theme.RedCard
import com.example.fourthofficial.ui.theme.YellowCard
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

@Composable
private fun playerTileColor(yellowActive: Boolean, redActive: Boolean) = when {
    redActive -> RedCard
    yellowActive -> YellowCard
    else -> MaterialTheme.colorScheme.surface
}

@Composable
fun TeamColumn(
    team: Team, modifier: Modifier = Modifier.Companion, vm: MatchViewModel,
    playerStates: Map<PlayerId, MatchPlayerState>, onPlayerTapped: (PlayerId) -> Unit,
    onPlayerLongPressed: (PlayerId) -> Unit, onPreparedSubstitutionsTapped: () -> Unit
) {
    val onField = team.players
        .mapNotNull { player -> playerStates[player.id]?.let { state -> player to state } }
        .filter { (_, state) -> state.isOnField }
        .sortedBy { (_, state) -> state.fieldPos ?: 999 }

    val preparedSubstitutionCount =
        vm.getPreparedSubstitutionBatch(team.id)?.substitutions?.size ?: 0

    TeamPanel(
        title = team.name.ifBlank { "Team ${team.index}" },
        modifier = modifier.padding(4.dp),
        headerAction =
            if (preparedSubstitutionCount > 0) {
                {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.clickable(onClick = onPreparedSubstitutionsTapped)
                    ) {
                        Text(
                            text = "SUBS $preparedSubstitutionCount",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                null
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            items(onField.size) { i ->
                val (player, state) = onField[i]
                val locked = !vm.canActOnPlayer(state)
                val tileContentColor = when {
                    state.isRedCarded -> OnRedCard
                    vm.isYellowActive(state) -> OnYellowCard
                    else -> MaterialTheme.colorScheme.onSurface
                }

                PlayerTile(
                    color = playerTileColor(
                        vm.isYellowActive(state),
                        state.isRedCarded
                    ),
                    contentColor = tileContentColor,
                    modifier = Modifier.then(
                        if (!locked) {
                            Modifier.combinedClickable(
                                onClick = { onPlayerTapped(player.id) },
                                onLongClick = { onPlayerLongPressed(player.id) }
                            )
                        } else {
                            Modifier
                        }
                    )
                ) {
                    Text(
                        text = "${player.number}. ${player.name}",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    when {
                        state.isRedCarded -> { Text(text = "RED", fontWeight = FontWeight.Bold) }
                        vm.isYellowActive(state) -> {
                            Text(
                                text = vm.formatClock(vm.yellowRemainingMs(state), true),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}