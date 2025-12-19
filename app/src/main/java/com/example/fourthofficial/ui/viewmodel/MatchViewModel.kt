package com.example.fourthofficial.ui.viewmodel

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fourthofficial.model.Player
import com.example.fourthofficial.model.Substitution
import com.example.fourthofficial.model.Team
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MatchClockState(
    val isRunning: Boolean = false,
    val elapsedMs: Long = 0L
)

class MatchViewModel : ViewModel() {

    var team1 by mutableStateOf(defaultTeam("Team 1", 1))
        private set

    var team2 by mutableStateOf(defaultTeam("Team 2", 2))
        private set

    var clock by mutableStateOf(MatchClockState())
        private set

    var subEvents = mutableStateListOf<Substitution>()
        private set

    fun defaultTeam(name: String, index: Int): Team =
        Team(
            name,
            index = index,
            List(23) { i ->
                val num = i + 1
                Player(
                    number = num,
                    name = "player $num",
                    isOnField = i < 15,
                    fieldPos = if (i < 15) num else null
                )
            }
        )

    fun updateTeam1(updated: Team) { team1 = updated }
    fun updateTeam2(updated: Team) { team2 = updated }

    fun recordSub(teamIndex: Int, offNumber: Int, onNumber: Int) {
        val t = clock.elapsedMs

        subEvents.add(
            Substitution(
                timeMs = t,
                teamIndex = teamIndex,
                playerOff = offNumber,
                playerOn = onNumber
            )
        )
    }

    fun makeSub(teamIndex: Int, offNumber: Int, onNumber: Int) {
        val team = if (teamIndex == 1) team1 else team2
        val offPos = team.players.firstOrNull{it.number == offNumber} ?.fieldPos ?: return

        val updatedPlayers = team.players.map { p ->
            when (p.number) {
                offNumber -> p.copy(isOnField = false, fieldPos = null)
                onNumber  -> p.copy(isOnField = true, fieldPos = offPos)
                else      -> p
            }
        }

        val updatedTeam = team.copy(players = updatedPlayers)
        if (teamIndex == 1) team1 = updatedTeam else team2 = updatedTeam

        // record the event after applying it
        recordSub(teamIndex, offNumber, onNumber)
    }
    private var startRealtimeMs: Long = 0L
    private var baseElapsedMs: Long = 0L
    private var tickerJob: Job? = null

    fun toggleClock() {
        if (clock.isRunning) stopClock() else startClock()
    }

    fun startClock() {
        if (clock.isRunning) return

        baseElapsedMs = clock.elapsedMs
        startRealtimeMs = SystemClock.elapsedRealtime()
        clock = clock.copy(isRunning = true)

        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val now = SystemClock.elapsedRealtime()
                val runningMs = now - startRealtimeMs
                clock = clock.copy(elapsedMs = baseElapsedMs + runningMs)
                delay(200)
            }
        }
    }

    fun stopClock() {
        if (!clock.isRunning) return

        val now = SystemClock.elapsedRealtime()
        val runningMs = now - startRealtimeMs

        tickerJob?.cancel()
        tickerJob = null

        clock = MatchClockState(
            isRunning = false,
            elapsedMs = baseElapsedMs + runningMs
        )
    }

    fun resetClock() {
        tickerJob?.cancel()
        tickerJob = null
        baseElapsedMs = 0L
        clock = MatchClockState()
    }

    fun formatClock(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
