package com.example.fourthofficial.ui.viewmodel

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fourthofficial.model.DiscReason
import com.example.fourthofficial.model.DiscType
import com.example.fourthofficial.model.Discipline
import com.example.fourthofficial.model.Player
import com.example.fourthofficial.model.Score
import com.example.fourthofficial.model.ScoreType
import com.example.fourthofficial.model.SubType
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

    var team1 by mutableStateOf(defaultTeam(1))
        private set

    var team2 by mutableStateOf(defaultTeam(2))
        private set

    var clock by mutableStateOf(MatchClockState())
        private set

    private var nextEventId: Int = 1
    private fun newID() = nextEventId++

    var scoreEvents = mutableStateListOf<Score>()
        private set

    var subEvents = mutableStateListOf<Substitution>()
        private set

    var discEvents = mutableStateListOf<Discipline>()
        private set

    var halfTimeMs = mutableLongStateOf(0L)
        private set

    private val halfDurationMs = 40L * 60L * 1000L

    private var matchOffsetMs by mutableLongStateOf(0L)

    val halfElapsedMs: Long
        get() = matchOffsetMs + clock.elapsedMs

    val halfRemainingMs: Long
        get() = (halfDurationMs - clock.elapsedMs )

    fun defaultTeam(index: Int): Team =
        Team(
            name = "",
            index = index,
            List(23) { i ->
                val num = i + 1
                Player(
                    number = num,
                    name = "",
                    isOnField = i < 15,
                    fieldPos = if (i < 15) num else null
                )
            }
        )

    fun updateTeam1(updated: Team) { team1 = updated }
    fun updateTeam2(updated: Team) { team2 = updated }

    fun recordScore(teamIndex: Int, playerNumber: Int, scoreType: ScoreType) {
        val t = halfElapsedMs

        scoreEvents.add(
            Score(
                eventID = newID(),
                timeMs = t,
                teamIndex = teamIndex,
                halfIndex = if(halfTimeMs.longValue == 0L) 1 else 2,
                player = playerNumber,
                type = scoreType
            )
        )
    }

    fun recordSub(teamIndex: Int, offNumber: Int, onNumber: Int, reason: SubType) {
        val t = halfElapsedMs

        subEvents.add(
            Substitution(
                eventID = newID(),
                timeMs = t,
                teamIndex = teamIndex,
                halfIndex = if(halfTimeMs.longValue == 0L) 1 else 2,
                playerOff = offNumber,
                playerOn = onNumber,
                type = reason
            )
        )
    }

    fun makeSub(teamIndex: Int, offNumber: Int, onNumber: Int, type: SubType) {
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

        recordSub(teamIndex, offNumber, onNumber, type)
    }

    fun recordDiscipline(teamIndex: Int, playerNumber: Int, type: DiscType, reason: DiscReason) {
        val t = halfElapsedMs

        val finalType =
            if (type == DiscType.YELLOW && discEvents.any { event ->
                event.teamIndex == teamIndex &&
                event.player == playerNumber &&
                event.type == DiscType.YELLOW })
            {
                DiscType.RED
            }
            else type

        discEvents.add(
            Discipline(
                eventID = newID(),
                timeMs = t,
                teamIndex = teamIndex,
                halfIndex = if(halfTimeMs.longValue == 0L) 1 else 2,
                player = playerNumber,
                type = type,
                reason = reason,
            )
        )

        if (finalType == DiscType.YELLOW)
            applyYellow(teamIndex, playerNumber)
        else
            applyRed(teamIndex, playerNumber)
    }

    fun logHalf() {
        if (clock.elapsedMs < halfDurationMs) return

        halfTimeMs.longValue = halfElapsedMs
        matchOffsetMs = halfDurationMs
        tickerJob?.cancel()
        tickerJob = null
        baseElapsedMs = 0L
        clock = MatchClockState()
    }

    fun resetScores() {
        scoreEvents.clear()
    }

    fun resetSubs() {
        subEvents.clear()
    }

    fun resetDiscs() {
        discEvents.clear()
        clearAllCards()
    }

    private val yellowDurationMs = 10L * 60L * 1000L

    fun yellowRemainingMs(player: Player): Long {
        val until = player.yellowUntilHalfMs ?: return 0L
        return (until - halfElapsedMs).coerceAtLeast(0L)
    }

    private fun applyYellow(teamIndex: Int, playerNumber: Int) {
        val team = if (teamIndex == 1) team1 else team2
        val until = halfElapsedMs + yellowDurationMs

        val updatedPlayers = team.players.map { player ->
            if (player.number == playerNumber) player.copy(yellowUntilHalfMs = until) else player
        }
        val updatedTeam = team.copy(players = updatedPlayers)
        if (teamIndex == 1) team1 = updatedTeam else team2 = updatedTeam
    }

    private fun applyRed(teamIndex: Int, playerNumber: Int) {
        val team = if (teamIndex == 1) team1 else team2

        val updatedPlayers = team.players.map { player ->
            if (player.number == playerNumber) {
                player.copy(
                    isRedCarded = true,
                    yellowUntilHalfMs = null,
                )
            } else player
        }

        val updatedTeam = team.copy(players = updatedPlayers)
        if (teamIndex == 1) team1 = updatedTeam else team2 = updatedTeam
    }

    fun isYellowActive(player: Player): Boolean {
        val until = player.yellowUntilHalfMs ?: return false
        return halfElapsedMs < until
    }

    fun isRedActive(player: Player):Boolean {
        return player.isRedCarded
    }

    private fun clearAllCards() {
        team1 = team1.copy(players = team1.players.map { it.copy(yellowUntilHalfMs = null, isRedCarded = false) })
        team2 = team2.copy(players = team2.players.map { it.copy(yellowUntilHalfMs = null, isRedCarded = false) })
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
                delay(100)
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

    fun isClockRunning(): Boolean {
        if(clock.isRunning)
            return true
        return false
    }

    fun resetClock() {
        tickerJob?.cancel()
        tickerJob = null
        baseElapsedMs = 0L
        clock = MatchClockState()
        matchOffsetMs = 0L
    }

    fun formatClock(ms: Long, remaining: Boolean): String {
        val remainingMs = 999 + ms
        var totalSeconds = ms / 1000
        if(remaining)
        {
            totalSeconds = remainingMs / 1000
        }
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
