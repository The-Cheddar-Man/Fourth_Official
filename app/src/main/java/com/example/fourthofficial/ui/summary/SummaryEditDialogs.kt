package com.example.fourthofficial.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.event.Score
import com.example.fourthofficial.domain.event.ScoreType
import com.example.fourthofficial.domain.event.Substitution
import com.example.fourthofficial.domain.event.SubstitutionType
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.team.Player

@Composable
fun EditScoreDialog(
    event: Score,
    players: List<Player>,
    initialTimeText: String,
    maxTimeMs: Long?,
    onSave: (playerId: PlayerId, type: ScoreType, timeMs: Long) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    var selectedPlayerId by remember(event.id.value) { mutableStateOf(event.playerId) }
    var selectedType by remember(event.id.value) { mutableStateOf(event.type) }
    var timeText by remember(event.id.value) { mutableStateOf(initialTimeText) }
    var showDeleteConfirmation by remember(event.id.value) { mutableStateOf(false) }
    val parsedTimeMs = parseMatchTime(timeText)
    val timeIsAfterCurrentMatch = maxTimeMs != null && parsedTimeMs != null && parsedTimeMs > maxTimeMs
    val timeIsValid = parsedTimeMs != null && !timeIsAfterCurrentMatch
    val selectedPlayer = players.find { it.id == selectedPlayerId }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Score") },
            text = { Text("Delete this score event?") },
            confirmButton = { Button(onClick = onDelete) { Text("Delete") } },
            dismissButton = { OutlinedButton(onClick = {
                showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Edit Score") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp))
            {
                SelectionField(
                    label = "Type",
                    value = selectedType.label,
                    options = ScoreType.entries,
                    optionLabel = { it.label },
                    onSelected = { selectedType = it }
                )

                SelectionField(
                    label = "Player",
                    value = selectedPlayer?.let(::playerLabel) ?: "Unknown player",
                    options = players.sortedBy { it.number },
                    optionLabel = ::playerLabel,
                    onSelected = { selectedPlayerId = it.id }
                )

                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it},
                    label = { Text("Time (MM:SS)") },
                    singleLine = true,
                    isError = timeText.isNotBlank() && !timeIsValid,
                    supportingText = {
                        when {
                            timeText.isNotBlank() && parsedTimeMs == null -> {
                                Text("Enter time as MM:SS")
                            }

                            timeIsAfterCurrentMatch -> {
                                Text("Time cannot be later than the current match clock.")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val timeMs = parsedTimeMs?.takeIf { timeIsValid } ?: return@Button
                    onSave(selectedPlayerId, selectedType, timeMs)
                },
                enabled = timeIsValid
            )
            { Text("Save") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp))
            {
                OutlinedButton(onClick = { showDeleteConfirmation = true }) { Text("Delete") }
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun EditSubstitutionDialog(
    event: Substitution,
    players: List<Player>,
    initialTimeText: String,
    maxTimeMs: Long?,
    errorMessage: String?,
    onSave: (playerOffId: PlayerId, playerOnId: PlayerId, type: SubstitutionType, timeMs: Long) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    var selectedPlayerOffId by remember(event.id.value) { mutableStateOf(event.playerOffId) }
    var selectedPlayerOnId by remember(event.id.value) { mutableStateOf(event.playerOnId) }
    var selectedType by remember(event.id.value) { mutableStateOf(event.type) }
    var timeText by remember(event.id.value) { mutableStateOf(initialTimeText) }
    var showDeleteConfirmation by remember(event.id.value) { mutableStateOf(false) }
    val parsedTimeMs = parseMatchTime(timeText)
    val timeIsAfterCurrentMatch = maxTimeMs != null && parsedTimeMs != null && parsedTimeMs > maxTimeMs
    val playersAreDifferent = selectedPlayerOffId != selectedPlayerOnId
    val timeIsValid = parsedTimeMs != null && !timeIsAfterCurrentMatch
    val canSave = timeIsValid && playersAreDifferent
    val selectedPlayerOff = players.find { it.id == selectedPlayerOffId }
    val selectedPlayerOn = players.find { it.id == selectedPlayerOnId }
    val sortedPlayers = players.sortedBy { it.number }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Substitution") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Delete this substitution event?")
                    if (errorMessage != null) { Text(errorMessage) }
                }
            },
            confirmButton = { Button(onClick = onDelete) { Text("Delete") } },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false })
                { Text("Cancel") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Edit Substitution") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SelectionField(
                    label = "Player Off",
                    value = selectedPlayerOff?.let(::playerLabel) ?: "Unknown player",
                    options = sortedPlayers,
                    optionLabel = ::playerLabel,
                    onSelected = { selectedPlayerOffId = it.id }
                )

                SelectionField(
                    label = "Reason",
                    value = selectedType.label,
                    options = SubstitutionType.entries,
                    optionLabel = { it.label },
                    onSelected = { selectedType = it }
                )

                SelectionField(
                    label = "Player On",
                    value = selectedPlayerOn?.let(::playerLabel) ?: "Unknown player",
                    options = sortedPlayers,
                    optionLabel = ::playerLabel,
                    onSelected = { selectedPlayerOnId = it.id }
                )

                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("Time (MM:SS)") },
                    singleLine = true,
                    isError = timeText.isNotBlank() && !timeIsValid,
                    supportingText = {
                        when {
                            timeText.isNotBlank() && parsedTimeMs == null -> {
                                Text("Enter time as MM:SS")
                            }

                            timeIsAfterCurrentMatch -> {
                                Text("Time cannot be later than the current match clock.")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (!playersAreDifferent) {
                    Text("Player Off and Player On must be different players.")
                }

                if (errorMessage != null) {
                    Text(errorMessage)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = {
                    val timeMs = parsedTimeMs?.takeIf { canSave } ?: return@Button
                    val savedTimeMs =
                        if (timeMs / 1000L == event.timeMs / 1000L) {
                            event.timeMs
                        } else {
                            timeMs
                        }

                    onSave(selectedPlayerOffId, selectedPlayerOnId,
                        selectedType, savedTimeMs)
                }
            )
            { Text("Save") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { showDeleteConfirmation = true }) { Text("Delete") }
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun <T> SelectionField(
    label: String,
    value: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth())
    {
        Text(label)

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(value)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun parseMatchTime(value: String): Long?
{
    val parts = value.trim().split(":")
    if (parts.size != 2) { return null }

    val minutes = parts[0].toLongOrNull() ?: return null
    val seconds = parts[1].toLongOrNull() ?: return null
    if (minutes < 0L) { return null }

    if (seconds !in 0L..59L) { return null }

    return (minutes * 60L + seconds) * 1000L
}

private fun playerLabel(player: Player): String
{
    return "${player.number}. " + player.name.ifBlank { "(Unnamed)" }
}