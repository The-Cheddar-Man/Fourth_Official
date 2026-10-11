package com.thecheddarman.fourthofficial.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thecheddarman.fourthofficial.ui.theme.AppButtonShape
import com.thecheddarman.fourthofficial.ui.theme.FourthOfficialTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    canContinueMatch: Boolean,
    onStartNewMatch: () -> Unit,
    onContinueMatch: () -> Unit,
    onManageTeams: () -> Unit,
    onMatchHistory: () -> Unit,
    onHelpSettings: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Fourth Official",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = onStartNewMatch,
                shape = AppButtonShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start New Match")
            }

            OutlinedButton(
                onClick = onContinueMatch,
                enabled = canContinueMatch,
                shape = AppButtonShape,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text("Continue Match")
            }

            OutlinedButton(
                onClick = onManageTeams,
                shape = AppButtonShape,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text("Manage Teams")
            }

            OutlinedButton(
                onClick = onMatchHistory,
                shape = AppButtonShape,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text("Match History")
            }

            OutlinedButton(
                onClick = onHelpSettings,
                shape = AppButtonShape,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text("Help / Settings")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FourthOfficialTheme {
        HomeScreen(
            canContinueMatch = false,
            onStartNewMatch = {},
            onContinueMatch = {},
            onManageTeams = {},
            onMatchHistory = {},
            onHelpSettings = {}
        )
    }
}