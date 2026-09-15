package com.example.fourthofficial.ui.match.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchPlayerState
import com.example.fourthofficial.domain.match.PreparedSubstitution
import com.example.fourthofficial.domain.team.Player
import com.example.fourthofficial.ui.match.SubstitutionPreparationUiState
import com.example.fourthofficial.ui.theme.OnRedCard
import com.example.fourthofficial.ui.theme.OnYellowCard
import com.example.fourthofficial.ui.theme.RedCard
import com.example.fourthofficial.ui.theme.SubstitutionPairColors
import com.example.fourthofficial.ui.theme.YellowCard
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

@Composable
fun SubstitutionPreparationContent(
    vm: MatchViewModel,
    teamId: TeamId,
    teamName: String,
    preparationState: SubstitutionPreparationUiState,
    onPreparationStateChange: (SubstitutionPreparationUiState) -> Unit,
    onReturnToMatch: () -> Unit,
    onDiscard: () -> Unit
) {
    val batch = vm.getPreparedSubstitutionBatch(teamId) ?: return
    val substitutionCount = batch.substitutions.size
    var replacementPickerFor by remember(teamId) { mutableStateOf<PlayerId?>(null) }
    var reasonPickerFor by remember(teamId) { mutableStateOf<PlayerId?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (preparationState) {
            SubstitutionPreparationUiState.SelectPlayers -> {
                SubstitutionPlayerSelection(
                    vm = vm,
                    teamId = teamId,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDiscard, modifier = Modifier.weight(1f))
                    {
                        Text("Discard")
                    }

                    OutlinedButton(
                        onClick = onReturnToMatch,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Return to Match")
                    }

                    Button(
                        onClick = {
                            onPreparationStateChange(SubstitutionPreparationUiState.AssignSubstitutions) },
                        enabled = substitutionCount > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Continue ($substitutionCount)")
                    }
                }
            }

            SubstitutionPreparationUiState.AssignSubstitutions -> {
                val allAssignmentsComplete =
                    batch.substitutions.isNotEmpty() &&
                            batch.substitutions.all { substitution ->
                                substitution.playerOnId != null &&
                                        substitution.type != null
                            }

                Text(
                    text = "$teamName — Assign Substitutions",
                    style = MaterialTheme.typography.titleMedium
                )

                SubstitutionAssignmentList(
                    vm = vm,
                    teamId = teamId,
                    substitutions = batch.substitutions,
                    onChooseReplacement = { playerOffId -> replacementPickerFor = playerOffId },
                    onChooseReason = { playerOffId -> reasonPickerFor = playerOffId },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onPreparationStateChange(SubstitutionPreparationUiState.SelectPlayers)
                                  },
                        modifier = Modifier.weight(1f)
                    )
                    { Text("Back") }

                    OutlinedButton(onClick = onReturnToMatch, modifier = Modifier.weight(1f))
                    {
                        Text("Return to Match")
                    }

                    Button(
                        onClick = {
                            vm.applyPreparedSubstitutionBatch(teamId)
                            if (vm.getPreparedSubstitutionBatch(teamId) == null)
                            {
                                onReturnToMatch()
                            }
                        },
                        enabled = allAssignmentsComplete,
                        modifier = Modifier.weight(1f)
                    )
                    { Text("Submit $substitutionCount") }
                }
            }
        }
    }
    val replacementPlayerOffId = replacementPickerFor
    val reasonPlayerOffId = reasonPickerFor

    if (reasonPlayerOffId != null) {
        SubstituteReasonDialogue(
            onConfirm = { substitutionType ->
                vm.setPreparedSubstitutionType(
                    teamId = teamId,
                    playerOffId = reasonPlayerOffId,
                    type = substitutionType
                )
                reasonPickerFor = null
            },
            onDismiss = { reasonPickerFor = null }
        )
    }

    if (replacementPlayerOffId != null) {
        val team = when (teamId) {
            vm.team1.id -> vm.team1
            vm.team2.id -> vm.team2
            else -> null
        }

        if (team != null) {
            val substitution = batch.substitutions.find { it.playerOffId == replacementPlayerOffId }
            val currentPlayerOn = substitution?.playerOnId?.let { playerOnId ->
                team.players.find { it.id == playerOnId } }

            val eligiblePlayers =
                (vm.eligiblePlayersOn(teamId) + listOfNotNull(currentPlayerOn))
                    .distinctBy { player -> player.id.value }
                    .sortedBy { player -> player.number }

            if (eligiblePlayers.isNotEmpty()) {
                val playerOff = team.players.find { it.id == replacementPlayerOffId }

                val playerOffLabel = playerOff?.let { player ->
                    "${player.number}. " + player.name.ifBlank { "(Unnamed)" } } ?: "Unknown player"

                SubstitutePlayerOnDialogue(
                    playerOffLabel = playerOffLabel,
                    potentialSubs = eligiblePlayers,
                    onConfirm = { playerOnId ->
                        vm.setPreparedSubstitutionPlayerOn(
                            teamId = teamId,
                            playerOffId = replacementPlayerOffId,
                            playerOnId = playerOnId
                        )

                        replacementPickerFor = null
                    },
                    onDismiss = {
                        replacementPickerFor = null
                    }
                )
            }
            else {
                AlertDialog(
                    containerColor = MaterialTheme.colorScheme.surface,
                    onDismissRequest = { replacementPickerFor = null },
                    title = { Text("Substitution")},
                    text = { Text("No eligible replacement players are available.") },
                    confirmButton = { Button(onClick = { replacementPickerFor = null })
                        {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SubstitutionPlayerSelection(
    vm: MatchViewModel,
    teamId: TeamId,
    modifier: Modifier = Modifier
) {
    val team = when (teamId) {
        vm.team1.id -> vm.team1
        vm.team2.id -> vm.team2
        else -> return
    }

    val playerStates = when (teamId) {
        vm.team1.id -> vm.team1PlayerStates
        vm.team2.id -> vm.team2PlayerStates
        else -> return
    }

    val batch = vm.getPreparedSubstitutionBatch(teamId) ?: return
    val selectedPlayerOffIds = batch.substitutions.map { it.playerOffId }.toSet()
    val selectedPlayerOnIds = batch.substitutions.mapNotNull { it.playerOnId }.toSet()
    val eligiblePlayerOffIds = vm.eligiblePlayersOff(teamId).map { it.id }.toSet()
    val onFieldPlayers = team.players
        .mapNotNull { player -> playerStates[player.id]?.let { state -> player to state } }
        .filter { (_, state) -> state.isOnField }
        .sortedBy { (_, state) -> state.fieldPos ?: Int.MAX_VALUE }

    val benchPlayers = team.players
        .mapNotNull { player -> playerStates[player.id]?.let { state -> player to state } }
        .filter { (_, state) -> !state.isOnField }
        .sortedBy { (player, _) -> player.number }

    val pairColorsByPlayerId = buildMap<PlayerId, Color> {
        batch.substitutions.forEachIndexed { index, substitution ->
                val playerOnId = substitution.playerOnId ?: return@forEachIndexed
                val color = SubstitutionPairColors[index % SubstitutionPairColors.size]

                put(substitution.playerOffId, color)
                put(playerOnId, color)
        }
    }

    Row(
        modifier = modifier
    ) {
        SubstitutionOnFieldColumn(
            players = onFieldPlayers,
            selectedPlayerOffIds = selectedPlayerOffIds,
            eligiblePlayerOffIds = eligiblePlayerOffIds,
            pairColorsByPlayerId = pairColorsByPlayerId,
            onSelectionChanged = { player, selected ->
                if (selected) {
                    vm.addPreparedSubstitution(teamId = teamId, playerOffId = player.id)
                } else {
                    vm.removePreparedSubstitution(teamId = teamId, playerOffId = player.id)
                }
            },
            vm = vm,
            modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp)
        )

        SubstitutionBenchColumn(
            players = benchPlayers,
            selectedPlayerOnIds = selectedPlayerOnIds,
            pairColorsByPlayerId = pairColorsByPlayerId,
            vm = vm,
            modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp)
        )
    }
}

@Composable
private fun SubstitutionOnFieldColumn(
    players: List<Pair<Player, MatchPlayerState>>,
    selectedPlayerOffIds: Set<PlayerId>,
    eligiblePlayerOffIds: Set<PlayerId>,
    pairColorsByPlayerId: Map<PlayerId, Color>,
    onSelectionChanged: (Player, Boolean) -> Unit,
    vm: MatchViewModel,
    modifier: Modifier = Modifier
) {
    TeamPanel(title = "ON FIELD", modifier = modifier)
    {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            items(
                items = players,
                key = { (player, _) -> player.id.value }
            )
            { (player, state) ->
                val selected = player.id in selectedPlayerOffIds
                val canSelect = selected || player.id in eligiblePlayerOffIds

                SubstitutionPlayerTile(
                    player = player,
                    state = state,
                    vm = vm,
                    selected = selected,
                    pairColor = pairColorsByPlayerId[player.id],
                    enabled = canSelect,
                    onClick = { onSelectionChanged(player, !selected) }
                )
            }
        }
    }
}

@Composable
private fun SubstitutionBenchColumn(
    players: List<Pair<Player, MatchPlayerState>>,
    selectedPlayerOnIds: Set<PlayerId>,
    pairColorsByPlayerId: Map<PlayerId, Color>,
    vm: MatchViewModel,
    modifier: Modifier = Modifier
) {
    TeamPanel(
        title = "BENCH",
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            items(
                items = players,
                key = { (player, _) -> player.id.value }
            )
            { (player, state) ->
                SubstitutionPlayerTile(
                    player = player,
                    state = state,
                    vm = vm,
                    selected = player.id in selectedPlayerOnIds,
                    pairColor = pairColorsByPlayerId[player.id],
                    enabled = true,
                    onClick = null
                )
            }
        }
    }
}

@Composable
private fun SubstitutionPlayerTile(
    player: Player,
    state: MatchPlayerState,
    vm: MatchViewModel,
    selected: Boolean,
    pairColor: Color?,
    enabled: Boolean,
    onClick: (() -> Unit)?
) {
    val yellowActive = vm.isYellowActive(state)
    val backgroundColor =
        when {
            state.isRedCarded -> RedCard
            yellowActive -> YellowCard
            selected && pairColor == null -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        }

    val contentColor =
        when {
            state.isRedCarded -> OnRedCard
            yellowActive -> OnYellowCard
            selected && pairColor == null -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        }

    val borderColor =
        when {
            pairColor != null -> pairColor
            selected -> MaterialTheme.colorScheme.primary
            else -> null
        }

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        border = borderColor?.let { BorderStroke(2.dp, it) },
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .alpha(if (enabled) { 1f } else { 0.45f })
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled, onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${player.number}. ${player.name}",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            when {
                state.isRedCarded -> { Text(text = "RED", fontWeight = FontWeight.Bold) }
                yellowActive -> { Text(text = vm.formatClock(vm.yellowRemainingMs(state), true), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SubstitutionAssignmentList(
    vm: MatchViewModel,
    teamId: TeamId,
    substitutions: List<PreparedSubstitution>,
    onChooseReplacement: (PlayerId) -> Unit,
    onChooseReason: (PlayerId) -> Unit,
    modifier: Modifier = Modifier
) {
    val team = when (teamId) {
        vm.team1.id -> vm.team1
        vm.team2.id -> vm.team2
        else -> return
    }

    val playersById = team.players.associateBy { player -> player.id }

    val playerStates = when (teamId) {
        vm.team1.id -> vm.team1PlayerStates
        vm.team2.id -> vm.team2PlayerStates
        else -> return
    }

    val orderedSubstitutions =
        substitutions.sortedBy { substitution ->
            playerStates[substitution.playerOffId]?.fieldPos ?: Int.MAX_VALUE
        }

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp))
    {
        items(
            items = orderedSubstitutions,
            key = { substitution -> substitution.playerOffId.value }
        ) { substitution ->
            val playerOff = playersById[substitution.playerOffId]
            val playerOn = substitution.playerOnId?.let { playerOnId -> playersById[playerOnId] }

            SubstitutionAssignmentRow(
                playerOffLabel =
                    playerOff?.let { player ->
                        "${player.number}. " +
                                player.name.ifBlank { "(Unnamed)" } } ?: "Unknown player",

                playerOnLabel =
                    playerOn?.let { player ->
                        "${player.number}. " +
                                player.name.ifBlank { "(Unnamed)" }
                    },

                reasonLabel = substitution.type?.label,
                onChooseReplacement = { onChooseReplacement(substitution.playerOffId) },
                onChooseReason = { onChooseReason(substitution.playerOffId) }
            )
        }
    }
}

@Composable
private fun SubstitutionAssignmentRow(
    playerOffLabel: String,
    playerOnLabel: String?,
    reasonLabel: String?,
    onChooseReplacement: () -> Unit,
    onChooseReason: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = playerOffLabel, style = MaterialTheme.typography.titleSmall)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onChooseReplacement, modifier = Modifier.weight(1f))
            { Text(playerOnLabel ?: "Choose replacement") }

            OutlinedButton(onClick = onChooseReason, modifier = Modifier.weight(1f))
            { Text(reasonLabel ?: "Choose reason")
            }
        }
    }
}