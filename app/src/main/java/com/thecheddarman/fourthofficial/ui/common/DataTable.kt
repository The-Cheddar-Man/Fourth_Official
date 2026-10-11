package com.thecheddarman.fourthofficial.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class TableColumn<T>(
    val header: String,
    val weight: Float,
    val value: (T) -> String
)

@Composable
fun <T> DataTable(
    events: List<T>,
    columns: List<TableColumn<T>>,
    modifier: Modifier = Modifier,
    keySelector: ((T) -> Any)? = null,
    onRowClick: ((T) -> Unit)? = null,
    emptyMessage: String = "No events recorded."
) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            DataTableHeader(columns = columns)
            HorizontalDivider()
        }

        if (events.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (keySelector == null) {
            items(events) { item ->
                DataTableRow(
                    item = item,
                    columns = columns,
                    onClick = onRowClick
                )
            }
        } else {
            items(
                items = events,
                key = { keySelector(it) }
            ) { item ->
                DataTableRow(
                    item = item,
                    columns = columns,
                    onClick = onRowClick
                )
            }
        }
    }
}

@Composable
private fun <T> DataTableHeader(columns: List<TableColumn<T>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        columns.forEach { column ->
            Text(
                text = column.header,
                modifier = Modifier.weight(column.weight),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun <T> DataTableRow(
    item: T,
    columns: List<TableColumn<T>>,
    onClick: ((T) -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .then(
                if (onClick != null) {
                Modifier.clickable { onClick(item) }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        columns.forEach { column ->
            Text(
                text = column.value(item),
                modifier = Modifier.weight(column.weight),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    HorizontalDivider()
}