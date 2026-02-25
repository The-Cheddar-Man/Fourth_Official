package com.example.fourthofficial.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.model.PendingSub

@Composable
fun SubBatchReviewDialog(
    subs: List<PendingSub>,
    labelForSub: (PendingSub) -> String,
    onRemove: (Int) -> Unit,
    onAddAnother: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Substitutions") },
        text = {
            if (subs.isEmpty()) {
                Text("No substitutions queued.")
            } else {
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(subs.size) { i ->
                        val sub = subs[i]
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(labelForSub(sub), Modifier.weight(1f))
                            OutlinedButton(onClick = { onRemove(sub.playerOff) }) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(enabled = subs.isNotEmpty(), onClick = onConfirm) { Text("Confirm") }
        },
        dismissButton = {
            Row {
                OutlinedButton(onClick = onAddAnother) { Text("Add another") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    )
}