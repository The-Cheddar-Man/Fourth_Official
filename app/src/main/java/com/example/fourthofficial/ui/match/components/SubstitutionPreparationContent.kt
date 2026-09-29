package com.example.fourthofficial.ui.match.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchPlayerState
import com.example.fourthofficial.domain.match.PreparedSubstitution
import com.example.fourthofficial.domain.rules.EventEditResult
import com.example.fourthofficial.domain.team.Player
import com.example.fourthofficial.ui.common.AppAlertDialog
import com.example.fourthofficial.ui.common.TeamPanel
import com.example.fourthofficial.ui.match.SubstitutionPreparationUiState
import com.example.fourthofficial.ui.theme.AppButtonShape
import com.example.fourthofficial.ui.theme.DestructiveRed
import com.example.fourthofficial.ui.theme.OnRedCard
import com.example.fourthofficial.ui.theme.OnYellowCard
import com.example.fourthofficial.ui.theme.RedCard
import com.example.fourthofficial.ui.theme.SubstitutionPairColors
import com.example.fourthofficial.ui.theme.SuccessGreen
import com.example.fourthofficial.ui.theme.YellowCard
import com.example.fourthofficial.ui.viewmodel.MatchViewModel
import kotlinx.coroutines.launch

@Composable
fun SubstitutionPreparationContent(
    vm: MatchViewModel,
    teamId: TeamId,
    preparationState: SubstitutionPreparationUiState,
    onPreparationStateChange: (SubstitutionPreparationUiState) -> Unit,
    onReturnToMatch: () -> Unit,
    onDiscard: () -> Unit
) {
    val focusedBatch = vm.getPreparedSubstitutionBatch(teamId)
    val substitutionCount = focusedBatch?.substitutions?.size ?: 0
    val initialReplacementPlayerOffId =
        when (preparationState) {
            SubstitutionPreparationUiState.SelectPlayers -> null
            is SubstitutionPreparationUiState.AssignSubstitutions -> preparationState.initialReplacementPlayerOffId
        }
    var replacementSelectionFor by remember(teamId, initialReplacementPlayerOffId) {
        mutableStateOf(initialReplacementPlayerOffId?.let { playerOffId ->
            SubstitutionSelectionTarget(teamId = teamId, playerOffId = playerOffId) }
        )
    }
    var reasonSelectionFor by remember(teamId) {
        mutableStateOf<SubstitutionSelectionTarget?>(null)
    }
    var swipeErrorMessage by remember(teamId) { mutableStateOf<String?>(null) }

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
                    OutlinedButton(onClick = onDiscard, modifier = Modifier.weight(1f), shape = AppButtonShape)
                    {
                        Text("Discard")
                    }

                    OutlinedButton(
                        onClick = onReturnToMatch,
                        modifier = Modifier.weight(1f),
                        shape = AppButtonShape
                    ) {
                        Text("Return to Match")
                    }

                    Button(
                        onClick = {
                            onPreparationStateChange(SubstitutionPreparationUiState.AssignSubstitutions()) },
                        enabled = substitutionCount > 0,
                        modifier = Modifier.weight(1f),
                        shape = AppButtonShape
                    ) {
                        Text("Continue ($substitutionCount)")
                    }
                }
            }

            is SubstitutionPreparationUiState.AssignSubstitutions -> {
                Text(
                    text = "Assign Substitutions",
                    style = MaterialTheme.typography.titleMedium
                )

                swipeErrorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                val orderedTeams = listOf(vm.team1, vm.team2)

                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    orderedTeams.forEach { team ->
                        val substitutions = vm.getPreparedSubstitutions(team.id)

                        SubstitutionAssignmentTeamSection(
                            vm = vm,
                            teamId = team.id,
                            teamName = team.name.ifBlank { "Team ${team.index}" },
                            substitutions = substitutions,

                            onChooseReplacement = { playerOffId ->
                                replacementSelectionFor =
                                    SubstitutionSelectionTarget(
                                        teamId = team.id,
                                        playerOffId = playerOffId
                                    )
                            },

                            onChooseReason = { playerOffId ->
                                reasonSelectionFor =
                                    SubstitutionSelectionTarget(
                                        teamId = team.id,
                                        playerOffId = playerOffId
                                    )
                            },

                            onCancelSubstitution = { playerOffId ->
                                swipeErrorMessage = null

                                vm.removePreparedSubstitution(
                                    teamId = team.id,
                                    playerOffId = playerOffId
                                )

                                if (vm.getPreparedSubstitutions(team.id).isEmpty()) {
                                    vm.cancelPreparedSubstitutionBatch(team.id)
                                }

                                if (noPreparedSubstitutionsRemain(vm)) {
                                    onReturnToMatch()
                                }
                            },

                            onSubmitSubstitution = { playerOffId ->
                                when (
                                    val result =
                                        vm.applyPreparedSubstitution(
                                            teamId = team.id,
                                            playerOffId = playerOffId
                                        )
                                ) {
                                    EventEditResult.Success -> {
                                        swipeErrorMessage = null

                                        if (noPreparedSubstitutionsRemain(vm)) {
                                            onReturnToMatch()
                                        }
                                        true
                                    }

                                    is EventEditResult.Failure -> {
                                        swipeErrorMessage = result.message
                                        false
                                    }
                                }
                            },

                            onSubmitAll = {
                                vm.applyPreparedSubstitutionBatch(team.id)

                                if (noPreparedSubstitutionsRemain(vm)) {
                                    onReturnToMatch()
                                }
                            },

                            modifier =
                                if (substitutions.isEmpty()) {
                                    Modifier.fillMaxWidth()
                                } else {
                                    Modifier.fillMaxWidth().weight(1f)
                                }
                        )
                    }
                }

                val enteredDirectly = preparationState.initialReplacementPlayerOffId != null

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!enteredDirectly) {
                        OutlinedButton(
                            onClick = { onPreparationStateChange(SubstitutionPreparationUiState.SelectPlayers) },
                            modifier = Modifier.weight(1f),
                            shape = AppButtonShape
                        ) {
                            Text("Back")
                        }
                    }

                    OutlinedButton(
                        onClick = onReturnToMatch,
                        modifier = Modifier.weight(1f),
                        shape = AppButtonShape
                    ) {
                        Text("Return to Match")
                    }
                }
            }
        }
    }

    val replacementTarget = replacementSelectionFor
    val reasonTarget = reasonSelectionFor

    if (reasonTarget != null) {
        SubstituteReasonDialogue(
            onConfirm = { substitutionType ->
                vm.setPreparedSubstitutionType(
                    teamId = reasonTarget.teamId,
                    playerOffId = reasonTarget.playerOffId,
                    type = substitutionType
                )

                reasonSelectionFor = null
            },
            onDismiss = {
                reasonSelectionFor = null
            }
        )
    }

    if (replacementTarget != null) {
        val selectionTeam =
            when (replacementTarget.teamId) {
                vm.team1.id -> vm.team1
                vm.team2.id -> vm.team2
                else -> null
            }

        val selectionBatch = vm.getPreparedSubstitutionBatch(replacementTarget.teamId)

        if (selectionTeam != null && selectionBatch != null) {
            val substitution = selectionBatch.substitutions.find {
                it.playerOffId == replacementTarget.playerOffId
            }
            val currentPlayerOn = substitution?.playerOnId?.let {
                    playerOnId -> selectionTeam.players.find { it.id == playerOnId }
            }

            val eligiblePlayers = (vm.eligiblePlayersOn(replacementTarget.teamId) +
                    listOfNotNull(currentPlayerOn))
                .distinctBy { player -> player.id.value }
                .sortedBy { player -> player.number }

            if (eligiblePlayers.isNotEmpty()) {
                val playerOff = selectionTeam.players.find { it.id == replacementTarget.playerOffId }

                val playerOffLabel = playerOff?.let {
                        player -> "${player.number}. " + player.name.ifBlank { "(Unnamed)" }
                } ?: "Unknown player"

                SubstitutePlayerOnDialogue(
                    playerOffLabel = playerOffLabel,
                    potentialSubs = eligiblePlayers,
                    onConfirm = { playerOnId ->
                        vm.setPreparedSubstitutionPlayerOn(
                            teamId = replacementTarget.teamId,
                            playerOffId = replacementTarget.playerOffId,
                            playerOnId = playerOnId
                        )

                        replacementSelectionFor = null
                        reasonSelectionFor = replacementTarget
                    },

                    onDismiss = { replacementSelectionFor = null }
                )
            } else {
                AppAlertDialog(
                    title = "Substitution",
                    onDismissRequest = { replacementSelectionFor = null },
                    text = { Text("No eligible replacement players are available.") },
                    confirmButton = {
                        Button(
                            onClick = { replacementSelectionFor = null },
                            shape = AppButtonShape
                        ) {
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

    val pairColorsByPlayerId = buildMap {
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
private fun SubstitutionAssignmentTeamSection(
    vm: MatchViewModel,
    teamId: TeamId,
    teamName: String,
    substitutions: List<PreparedSubstitution>,
    onChooseReplacement: (PlayerId) -> Unit,
    onChooseReason: (PlayerId) -> Unit,
    onCancelSubstitution: (PlayerId) -> Unit,
    onSubmitSubstitution: (PlayerId) -> Boolean,
    onSubmitAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allAssignmentsComplete = substitutions.isNotEmpty() &&
            substitutions.all { substitution ->
                substitution.playerOnId != null && substitution.type != null
            }

    val title =
        if (substitutions.isEmpty())
            teamName
        else
            "$teamName (${substitutions.size})"

    TeamPanel(
        title = title,
        headerContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            OutlinedButton(
                onClick = onSubmitAll,
                enabled = allAssignmentsComplete,
                shape = AppButtonShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
            ) {
                Text(
                    text = "Submit",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        modifier = modifier
    ) {
        if (substitutions.isEmpty()) {
            Text(
                text = "No substitutions prepared",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
        } else {
            SubstitutionAssignmentList(
                vm = vm,
                teamId = teamId,
                substitutions = substitutions,
                onChooseReplacement = onChooseReplacement,
                onChooseReason = onChooseReason,
                onCancelSubstitution = onCancelSubstitution,
                onSubmitSubstitution = onSubmitSubstitution,
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(horizontal = 4.dp)
            )
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
    onCancelSubstitution: (PlayerId) -> Unit,
    onSubmitSubstitution: (PlayerId) -> Boolean,
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

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp))
    {
        items(
            items = orderedSubstitutions,
            key = { substitution -> substitution.playerOffId.value }
        ) { substitution ->
            val playerOff = playersById[substitution.playerOffId]
            val playerOn = substitution.playerOnId?.let { playerOnId -> playersById[playerOnId] }
            val assignmentComplete = substitution.playerOnId != null && substitution.type != null

            SwipeableSubstitutionAssignmentRow(
                canSubmit = assignmentComplete,
                onCancel = { onCancelSubstitution(substitution.playerOffId) },
                onSubmit = { onSubmitSubstitution(substitution.playerOffId) }
            ) {
                SubstitutionAssignmentRow(
                    playerOffLabel =
                        playerOff?.let { player ->
                            "${player.number}. " +
                                    player.name.ifBlank { "(Unnamed)" } } ?: "Unknown player",

                    playerOnLabel =
                        playerOn?.let { player ->
                            "${player.number}. " + player.name.ifBlank { "(Unnamed)" }
                        },

                    reasonLabel = substitution.type?.label,
                    onChooseReplacement = { onChooseReplacement(substitution.playerOffId) },
                    onChooseReason = { onChooseReason(substitution.playerOffId) }
                )
            }
        }
    }
}

@Composable
private fun SwipeableSubstitutionAssignmentRow(
    canSubmit: Boolean,
    onCancel: () -> Unit,
    onSubmit: () -> Boolean,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = canSubmit,
        enableDismissFromEndToStart = true,
        onDismiss = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    val success = onSubmit()
                    if (!success) {
                        scope.launch { dismissState.reset() }
                    }
                }
                SwipeToDismissBoxValue.EndToStart -> { onCancel() }
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val targetColor =
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> SuccessGreen
                    SwipeToDismissBoxValue.EndToStart -> DestructiveRed
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surface
                }

            val backgroundColor =
                lerp(
                    MaterialTheme.colorScheme.surface,
                    targetColor,
                    dismissState.progress.coerceIn(0f, 1f)
                )

            Box(
                modifier = Modifier.fillMaxSize().background(backgroundColor).padding(horizontal = 16.dp),
                contentAlignment =
                    when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        SwipeToDismissBoxValue.Settled -> Alignment.Center
                    }
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Text(
                            text = "SUBMIT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        Text(
                            text = "CANCEL",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    SwipeToDismissBoxValue.Settled -> Unit
                }
            }
        }
    ) {
        content()
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = playerOffLabel, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onChooseReplacement, modifier = Modifier.weight(1f), shape = AppButtonShape)
            { Text(playerOnLabel ?: "Choose replacement") }

            OutlinedButton(onClick = onChooseReason, modifier = Modifier.weight(1f), shape = AppButtonShape)
            { Text(reasonLabel ?: "Choose reason")
            }
        }
    }
}

private data class SubstitutionSelectionTarget(val teamId: TeamId, val playerOffId: PlayerId)

private fun noPreparedSubstitutionsRemain(vm: MatchViewModel): Boolean {
    return vm.getPreparedSubstitutions(vm.team1.id).isEmpty() &&
            vm.getPreparedSubstitutions(vm.team2.id).isEmpty()
}
