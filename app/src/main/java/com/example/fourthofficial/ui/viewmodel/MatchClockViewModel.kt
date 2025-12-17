package com.example.fourthofficial.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MatchClockState(
    val isRunning: Boolean = false,
    val elapsedMs: Long = 0L
)

class MatchClockViewModel : ViewModel() {

    private val _state = MutableStateFlow(MatchClockState())
    val state: StateFlow<MatchClockState> = _state.asStateFlow()

    private var startRealtimeMs: Long = 0L
    private var baseElapsedMs: Long = 0L
    private var tickerJob: Job? = null

    fun toggle() {
        if (_state.value.isRunning) stop() else start()
    }

    fun start() {
        if (_state.value.isRunning) return

        baseElapsedMs = _state.value.elapsedMs
        startRealtimeMs = SystemClock.elapsedRealtime()

        _state.value = _state.value.copy(isRunning = true)

        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val now = SystemClock.elapsedRealtime()
                val runningMs = now - startRealtimeMs
                _state.value = _state.value.copy(elapsedMs = baseElapsedMs + runningMs)
                delay(200) // or 1000 for 1/sec updates
            }
        }
    }

    fun stop() {
        if (!_state.value.isRunning) return

        val now = SystemClock.elapsedRealtime()
        val runningMs = now - startRealtimeMs

        tickerJob?.cancel()
        tickerJob = null

        _state.value = MatchClockState(
            isRunning = false,
            elapsedMs = baseElapsedMs + runningMs
        )
    }

    fun reset() {
        tickerJob?.cancel()
        tickerJob = null
        baseElapsedMs = 0L
        _state.value = MatchClockState()
    }
}
