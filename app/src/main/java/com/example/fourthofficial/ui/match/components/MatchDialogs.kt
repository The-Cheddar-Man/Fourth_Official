package com.example.fourthofficial.ui.match.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fourthofficial.domain.event.DisciplineReason
import com.example.fourthofficial.domain.event.DisciplineReasonRed
import com.example.fourthofficial.domain.event.DisciplineReasonYellow
import com.example.fourthofficial.domain.event.DisciplineType
import com.example.fourthofficial.domain.event.ScoreType
import com.example.fourthofficial.domain.event.SubstitutionType
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.team.Player
import com.example.fourthofficial.ui.common.AppAlertDialog
import com.example.fourthofficial.ui.common.SingleChoiceDialog
import com.example.fourthofficial.ui.theme.AppButtonShape
import com.example.fourthofficial.ui.theme.AppDialogShape

@Composable
fun ActionMenuDialogue(
    playerName: String,
    onScore: () -> Unit,
    onSubstitution: () -> Unit,
    onDiscipline: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = AppDialogShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Select Action For $playerName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Button(onClick = onScore, shape = AppButtonShape) { Text("Score") }
                Button(onClick = onSubstitution, shape = AppButtonShape) { Text("Substitution") }
                Button(onClick = onDiscipline, shape = AppButtonShape) { Text("Discipline") }
            }
        }
    }
}

@Composable
fun ScoreDialogue(
    teamName: String,
    playerName: String,
    onConfirm: (ScoreType) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: ScoreType? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Scoring",
        prompt = "$playerName ($teamName) scored:",
        options = ScoreType.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun SubstitutePlayerOnDialogue(
    playerOffLabel: String,
    potentialSubs: List<Player>,
    onConfirm: (PlayerId) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: Player? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Substitution",
        prompt = "Substitute $playerOffLabel for:",
        options = potentialSubs,
        selected = selected,
        optionLabel = { player -> "${player.number}. ${player.name.ifBlank { "(Unnamed)" }}" },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it.id) },
        onDismiss = onDismiss
    )
}

@Composable
fun SubstituteReasonDialogue(
    onConfirm: (SubstitutionType) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: SubstitutionType? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Substitution",
        prompt = "Reason for substitution:",
        options = SubstitutionType.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun DisciplineTypeDialogue(
    playerName: String,
    onConfirm: (DisciplineType) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: DisciplineType? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Discipline",
        prompt = "Card for $playerName:",
        options = DisciplineType.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun DisciplineReasonYellowDialogue(
    onConfirm: (DisciplineReason) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: DisciplineReason? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Discipline",
        prompt = "Discipline reason:",
        options = DisciplineReasonYellow.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun DisciplineReasonRedDialogue(
    onConfirm: (DisciplineReason) -> Unit,
    onDismiss: () -> Unit
) {
    var selected: DisciplineReason? by remember { mutableStateOf(null) }

    SingleChoiceDialog(
        title = "Discipline",
        prompt = "Discipline reason:",
        options = DisciplineReasonRed.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = { selected = it },
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun StartNewMatchDialogue(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = "Start New Match",
        text = {
            Text("Are you sure you want to start a new match?")
        },
        confirmButton = {
            Button(onClick = onConfirm, shape = AppButtonShape) { Text("Yes, New Game") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = AppButtonShape) { Text("Cancel") }
        }
    )
}

@Composable
fun FinishHalfDialogue(
    canFinishHalf: Boolean,
    finishingMatch: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = if (finishingMatch) "End Match" else "Log Half",
        text = { Text(
                when {
                    !canFinishHalf -> "This half is not over!"
                    finishingMatch -> "End the match?"
                    else -> "Log the first half?"
                }
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = canFinishHalf, shape = AppButtonShape) {
                Text(if (finishingMatch) "End Match" else "Log Half")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss, shape = AppButtonShape) {
            Text("Cancel")
        } }
    )
}