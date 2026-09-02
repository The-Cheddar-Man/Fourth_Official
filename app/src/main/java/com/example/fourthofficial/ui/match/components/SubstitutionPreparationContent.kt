package com.example.fourthofficial.ui.match.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.PreparedSubstitution
import com.example.fourthofficial.domain.team.Player
import com.example.fourthofficial.ui.match.SubstitutionPreparationUiState
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
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (preparationState) {
            SubstitutionPreparationUiState.SelectPlayers -> {
                Text(
                    text = "$teamName — Prepare Substitutions",
                    style = MaterialTheme.typography.titleMedium
                )

                SubstitutionPlayerSelection(
                    vm = vm,
                    teamId = teamId,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
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

    val eligiblePlayerOffIds = vm.eligiblePlayersOff(teamId).map { it.id }.toSet()

    val onFieldPlayers = team.players.mapNotNull {
        player -> playerStates[player.id]?.let { state -> player to state } }
        .filter { (_, state) -> state.isOnField }
        .sortedBy { (_, state) -> state.fieldPos ?: Int.MAX_VALUE }
        .map { (player, _) -> player }

    val benchPlayers = team.players
            .filter { player -> playerStates[player.id]?.isOnField == false }
            .sortedBy { player -> player.number }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SubstitutionOnFieldColumn(
            players = onFieldPlayers,
            selectedPlayerOffIds = selectedPlayerOffIds,
            eligiblePlayerOffIds = eligiblePlayerOffIds,
            onSelectionChanged = { player, selected ->
                if (selected) {
                    vm.addPreparedSubstitution(teamId = teamId, playerOffId = player.id)
                } else {
                    vm.removePreparedSubstitution(teamId = teamId, playerOffId = player.id)
                }
            },
            modifier = Modifier.weight(1f) .fillMaxHeight()
        )

        SubstitutionBenchColumn(
            players = benchPlayers,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}

@Composable
private fun SubstitutionOnFieldColumn(
    players: List<Player>,
    selectedPlayerOffIds: Set<PlayerId>,
    eligiblePlayerOffIds: Set<PlayerId>,
    onSelectionChanged: (Player, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "On Field",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = players, key = { player -> player.id.value })
            { player ->
                val selected = player.id in selectedPlayerOffIds
                val canSelect = selected || player.id in eligiblePlayerOffIds

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = selected,
                            enabled = canSelect,
                            role = Role.Checkbox,
                            onValueChange = { checked -> onSelectionChanged(player, checked) }
                        ).padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = null,
                        enabled = canSelect
                    )

                    Text(
                        text = "${player.number}. " + player.name.ifBlank { "(Unnamed)" },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SubstitutionBenchColumn(players: List<Player>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "Bench",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = players, key = { player -> player.id.value })
            { player ->
                Text(text = "${player.number}. " + player.name.ifBlank { "(Unnamed)" },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)
                )
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