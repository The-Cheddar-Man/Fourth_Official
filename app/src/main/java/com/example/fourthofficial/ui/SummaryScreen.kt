package com.example.fourthofficial.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class SummaryTab {
    Scores, Substitutions, Disciplines, Export
}

@Composable
fun SummaryScreen(modifier: Modifier = Modifier) {
    var currentTab by rememberSaveable { mutableStateOf(SummaryTab.Substitutions) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = currentTab.ordinal
        ) {
            SummaryTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = currentTab.ordinal == index,
                    onClick = { currentTab = tab },
                    text = {
                        Text(
                            tab.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        when (currentTab) {
            SummaryTab.Scores -> ScoresTab()
            SummaryTab.Substitutions -> SubstitutionsTab()
            SummaryTab.Disciplines -> DisciplinesTab()
            SummaryTab.Export -> ExportTab()
        }
    }
}

@Composable
private fun ScoresTab(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Match Scores", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun SubstitutionsTab(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Match Substitutions", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun DisciplinesTab(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Match Disciplines", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun ExportTab(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Export Data", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreview() {
    SummaryScreen()
}