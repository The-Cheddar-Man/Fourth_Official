package com.example.fourthofficial.ui.setup

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.team.Player
import com.example.fourthofficial.domain.team.Team
import com.example.fourthofficial.ui.match.components.StartNewMatchDialogue
import com.example.fourthofficial.ui.common.TeamPanel
import com.example.fourthofficial.ui.theme.AppButtonShape
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

@Composable
fun MatchSetupScreen(modifier: Modifier = Modifier.Companion, vm: MatchViewModel,
                     onTeam1Change: (Team) -> Unit, onTeam2Change: (Team) -> Unit)
{
    var showResetDialog by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxSize())
    {
        Row(modifier = Modifier.weight(1f).fillMaxWidth())
        {
            TeamColumn(
                team = vm.team1,
                onSave = onTeam1Change,
                modifier = Modifier.weight(1f)
            )

            TeamColumn(
                team = vm.team2,
                onSave = onTeam2Change,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = { showResetDialog = true },
            shape = AppButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) { Text("Start New Match") }
    }

    if (showResetDialog) {
        StartNewMatchDialogue(
            onConfirm = {
                vm.startNewMatch()
                showResetDialog = false
            },
            onDismiss = {
                showResetDialog = false
            }
        )
    }
}

@Composable
private fun TeamColumn(team: Team, onSave: (Team) -> Unit, modifier: Modifier = Modifier) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember(team) { mutableStateOf(team.name) }
    val editedPlayers = remember(team) { mutableStateListOf(*team.players.toTypedArray()) }

    LaunchedEffect(team, isEditing) {
        if (!isEditing) {
            editedName = team.name
            editedPlayers.clear()
            editedPlayers.addAll(team.players)
        }
    }

    TeamPanel(
        title = team.name.ifBlank { "Team ${team.index}" },
        headerContent = if (isEditing) {
                {
                    BasicTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        textStyle =
                            MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
                        modifier = Modifier.weight(1f).border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.25f),
                            shape = MaterialTheme.shapes.extraSmall
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (editedName.isBlank()) {
                                    Text(
                                        text = "Team ${team.index}",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                innerTextField()
                            }
                        }
                    )
                }
            } else { null },
        modifier = modifier.fillMaxHeight().padding(4.dp)
        ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                count = editedPlayers.size,
                key = { index -> editedPlayers[index].id.value }
            ) { index ->
                EditPlayerRow(
                    index = index,
                    player = editedPlayers[index],
                    isEditing = isEditing,
                    onPlayerChange = { updated -> editedPlayers[index] = updated }
                )
            }
        }

        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        editedName = team.name
                        editedPlayers.clear()
                        editedPlayers.addAll(team.players)
                        isEditing = false
                    },
                    shape = AppButtonShape,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Cancel", maxLines = 1)
                }

                Button(
                    onClick = {
                        onSave(team.copy(name = editedName, players = editedPlayers.toList()))
                        isEditing = false
                    },
                    shape = AppButtonShape,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Save", maxLines = 1)
                }
            }
        } else {
            Button(
                onClick = { isEditing = true },
                shape = AppButtonShape,
                modifier = Modifier.fillMaxWidth().padding(6.dp)
            ) {
                Text("Edit Team")
            }
        }
    }
}

@Composable
private fun EditPlayerRow(index: Int, player: Player, isEditing: Boolean, onPlayerChange: (Player) -> Unit) {
    var playerNum by remember(player.number) { mutableStateOf(player.number.toString()) }
    val textColor = MaterialTheme.colorScheme.onSurface

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        border =
            if (isEditing) {
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            } else {
                null
            },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = playerNum,
                onValueChange = { newText ->
                    val filtered = newText.filter { it.isDigit() }.take(3)
                    playerNum = filtered

                    filtered.toIntOrNull()?.let { number ->
                        onPlayerChange(player.copy(number = number))
                    }
                },
                readOnly = !isEditing,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle =
                    MaterialTheme.typography.bodyLarge.copy(
                        color = textColor,
                        textAlign = TextAlign.Center
                    ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.width(32.dp)
            )

            Text(text = ".", style = MaterialTheme.typography.bodyLarge, color = textColor)

            Spacer(Modifier.width(8.dp))

            BasicTextField(
                value = player.name,
                onValueChange = { newName -> onPlayerChange(player.copy(name = newName)) },
                readOnly = !isEditing,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (player.name.isBlank()) {
                            Text(
                                text = "Player ${index + 1}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        innerTextField()
                    }
                }
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun MatchSetupScreenPreview() {
    MatchSetupScreen(
        vm = MatchViewModel(),
        onTeam1Change = {},
        onTeam2Change = {}
    )
}