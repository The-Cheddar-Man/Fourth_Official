package com.example.fourthofficial.ui

import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment

@Composable
fun SetupScreen(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(5.dp)
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("Team 1", style = MaterialTheme.typography.headlineMedium)
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(23) { i ->
                    Text("Player ${i + 1}", modifier = Modifier.padding(2.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Edit Team")
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(5.dp)
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("Team 2", style = MaterialTheme.typography.headlineMedium)
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(23) { i ->
                    Text("Player ${i + 1}", modifier = Modifier.padding(2.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Edit Team")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SetupScreenPreview() {
    SetupScreen()
}