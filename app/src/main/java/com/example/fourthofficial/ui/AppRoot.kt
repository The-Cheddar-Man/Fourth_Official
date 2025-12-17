package com.example.fourthofficial.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

private enum class Screen {
    Setup, Match, Summary
}

@Composable
fun AppRoot() {
    var screen by rememberSaveable { mutableStateOf(Screen.Setup) }
    val matchVm: MatchViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = screen == Screen.Setup,
                    onClick = { screen = Screen.Setup },
                    label = { Text("Setup") },
                    icon = {}
                )
                NavigationBarItem(
                    selected = screen == Screen.Match,
                    onClick = { screen = Screen.Match },
                    label = { Text("Match") },
                    icon = {}
                )
                NavigationBarItem(
                    selected = screen == Screen.Summary,
                    onClick = { screen = Screen.Summary },
                    label = { Text("Summary") },
                    icon = {}
                )
            }
        }
    ) { padding ->

        when (screen) {
            Screen.Setup -> SetupScreen(
                modifier = Modifier.padding(padding),
                vm = matchVm,
                onTeam1Change = matchVm::updateTeam1,
                onTeam2Change = matchVm::updateTeam2
            )
            Screen.Match -> MatchScreen(
                modifier = Modifier.padding(padding),
                vm = matchVm
            )
            Screen.Summary -> SummaryScreen(
                modifier = Modifier.padding(padding)
            )
        }
    }
}