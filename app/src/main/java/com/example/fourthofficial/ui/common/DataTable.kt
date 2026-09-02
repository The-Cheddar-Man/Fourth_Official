package com.example.fourthofficial.ui.common

import androidx.compose.foundation.clickable
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
    modifier: Modifier = Modifier,
    keySelector: ((T) -> Any)? = null,
    onRowClick: ((T) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            Row(Modifier.fillMaxWidth()) {
                columns.forEach { col ->
                    Text(
                        col.header,
                        modifier = Modifier.weight(col.weight),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HorizontalDivider()
        }

        if (keySelector == null) {
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
private fun <T> DataTableRow(
    item: T,
    columns: List<TableColumn<T>>,
    onClick: ((T) -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth().then(
            if (onClick != null) {
                Modifier.clickable { onClick(item) }
            }
            else {
                Modifier
            }
        )
    ) {
        columns.forEach { col ->
            Text(
                text = col.value(item),
                modifier = Modifier.weight(col.weight),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    HorizontalDivider()
}