package com.thecheddarman.fourthofficial

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thecheddarman.fourthofficial.ui.home.HomeScreen
import com.thecheddarman.fourthofficial.ui.matchflow.MatchFlow
import com.thecheddarman.fourthofficial.ui.matchflow.MatchFlowScreen

@Composable
fun AppRoot() {
    var destination by rememberSaveable { mutableStateOf(AppDestination.Home) }

    fun navigateTo(newDestination: AppDestination) {
        destination = newDestination
    }

    when (destination) {
        AppDestination.Home -> HomeScreen(
            canContinueMatch = false,
            onStartNewMatch = { navigateTo(AppDestination.MatchFlow) },
            onContinueMatch = { navigateTo(AppDestination.MatchFlow) },
            onManageTeams = { navigateTo(AppDestination.ManageTeams) },
            onMatchHistory = { navigateTo(AppDestination.MatchHistory) },
            onHelpSettings = { navigateTo(AppDestination.HelpSettings) }
        )

        AppDestination.ManageTeams -> {
            BackHandler { navigateTo(AppDestination.Home) }
            PlaceholderScreen(text = "Manage Teams\nComing Soon!")
        }
        AppDestination.MatchFlow -> MatchFlow(
            initialScreen = MatchFlowScreen.Setup,
            onExitToHome = { navigateTo(AppDestination.Home) }
        )
        AppDestination.MatchHistory -> {
            BackHandler { navigateTo(AppDestination.Home) }
            PlaceholderScreen(text = "Match History\nComing Soon!")
        }
        AppDestination.HelpSettings -> {
            BackHandler { navigateTo(AppDestination.Home) }
            PlaceholderScreen(text = "Help / Settings\nComing Soon!")
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}