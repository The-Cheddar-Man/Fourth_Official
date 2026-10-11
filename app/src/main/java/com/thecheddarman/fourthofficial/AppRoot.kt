package com.thecheddarman.fourthofficial

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thecheddarman.fourthofficial.ui.match.MatchScreen
import com.thecheddarman.fourthofficial.ui.setup.MatchSetupScreen
import com.thecheddarman.fourthofficial.ui.summary.SummaryScreen
import com.thecheddarman.fourthofficial.ui.viewmodel.MatchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private enum class Screen {
    Setup, Match, Summary
}

@Composable
fun AppRoot() {
    var screen by rememberSaveable { mutableStateOf(Screen.Setup) }
    fun setScreen(newScreen:Screen){screen = newScreen}
    val matchVm: MatchViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun showMessage(message: String, durationMs: Long) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()

            launch {
                delay(durationMs.milliseconds)
                snackbarHostState.currentSnackbarData?.dismiss()
            }

            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Indefinite
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Surface(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).wrapContentWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = data.visuals.message,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AppNavigationBarItem(
                    selected = screen == Screen.Setup,
                    onClick = { setScreen(Screen.Setup) },
                    label = "Setup",
                    onDevHold = {
                        val error = matchVm.loadDevTestTeam()
                        showMessage(error ?: "Dev teams loaded", 2_000L)
                    }
                )
                AppNavigationBarItem(
                    selected = screen == Screen.Match,
                    onClick = { setScreen(Screen.Match) },
                    label = "Match",
                    onDevHold = {
                        val error = matchVm.loadDevTestMatch()
                        showMessage(error ?: "Dev match loaded", 2_000L)
                    }
                )
                AppNavigationBarItem(
                    selected = screen == Screen.Summary,
                    onClick = { setScreen(Screen.Summary) },
                    label = "Summary",
                    onDevHold = {
                        val error = matchVm.loadDevTestSummary()
                        showMessage(error ?: "Dev match history loaded", 2_000L)
                    }
                )
            }
        }
    ) { padding ->

        when (screen) {
            Screen.Setup -> MatchSetupScreen(
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
                modifier = Modifier.padding(padding),
                vm = matchVm,
                onShowMessage = { message ->
                    showMessage(message, 4_000L)
                }
            )
        }
    }
}

@Composable
private fun RowScope.AppNavigationBarItem(selected: Boolean, label: String,
                                 onClick: () -> Unit, onDevHold: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnDevHold by rememberUpdatedState(onDevHold)

    if (BuildConfig.DEBUG) {
        LaunchedEffect(interactionSource) {
            var holdJob: Job? = null

            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        holdJob?.cancel()
                        holdJob = this@LaunchedEffect.launch {
                            delay(5_000L.milliseconds)
                            currentOnDevHold()
                        }
                    }

                    is PressInteraction.Release,
                    is PressInteraction.Cancel -> {
                        holdJob?.cancel()
                        holdJob = null
                    }
                }
            }
        }
    }

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        icon = {},
        interactionSource = interactionSource
    )
}