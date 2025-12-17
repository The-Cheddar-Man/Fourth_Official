package com.example.fourthofficial.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MatchScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Match Screen", style = MaterialTheme.typography.headlineMedium)
        Text("00:00", style = MaterialTheme.typography.displayMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchScreenPreview() {
    MatchScreen()
}