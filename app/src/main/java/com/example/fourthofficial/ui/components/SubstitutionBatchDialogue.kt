package com.example.fourthofficial.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fourthofficial.model.PendingSub

@Composable
fun SubBatchReviewDialog(
    subs: List<PendingSub>,
    labelForSub: (PendingSub) -> String,
    onRemove: (Int) -> Unit,
    onAddAnother: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    eligibleOn: Boolean
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Substitutions", style = MaterialTheme.typography.titleLarge)
                if (subs.isEmpty()) {
                    Text("No substitutions queued.")
                } else {
                    LazyColumn(Modifier.heightIn(max = 320.dp)) {
                        items(subs.size) { i ->
                            val sub = subs[i]
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onCancel) {
                        Text("Cancel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .widthIn(max = 180.dp),
                        enabled = eligibleOn,
                        onClick = onAddAnother
                    ) {
                        Text("Add another", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Button(
                        modifier = Modifier.widthIn(min = 110.dp),
                        enabled = subs.isNotEmpty(),
                        onClick = onConfirm
                    ) {
                        Text("Confirm", maxLines = 1)
                    }
                }
            }
        }
    }
}
