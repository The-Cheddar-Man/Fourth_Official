package com.example.fourthofficial.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

data class TableColumn<T>(
    val header: String,
    val weight: Float,
    val value: (T) -> String
)

@Composable
fun <T> DataTable(
    events: List<T>,
    columns: List<TableColumn<T>>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            Row(Modifier.fillMaxWidth()) {
                columns.forEach { col ->
                    Text(col.header, modifier = Modifier.weight(col.weight), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            HorizontalDivider()
        }

        items(
            items = events,
            key = { it.hashCode() }
        ) { item ->
            Row(Modifier.fillMaxWidth()) {
                columns.forEach { col ->
                    Text(col.value(item), modifier = Modifier.weight(col.weight), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            HorizontalDivider()
        }
    }
}