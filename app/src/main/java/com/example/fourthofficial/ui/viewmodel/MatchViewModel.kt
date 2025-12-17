package com.example.fourthofficial.ui.viewmodel

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fourthofficial.model.Team
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MatchClockState(
    val isRunning: Boolean = false,
    val elapsedMs: Long = 0L
)

class MatchViewModel : ViewModel() {

    var team1 by mutableStateOf(Team("Team 1", List(23) { "" }))
        private set

    var team2 by mutableStateOf(Team("Team 2", List(23) { "" }))
        private set

    fun updateTeam1(updated: Team) { team1 = updated }
    fun updateTeam2(updated: Team) { team2 = updated }

    var clock by mutableStateOf(MatchClockState())
        private set

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
}
