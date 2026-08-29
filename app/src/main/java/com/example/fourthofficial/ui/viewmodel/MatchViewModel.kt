package com.example.fourthofficial.ui.viewmodel

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchClock
import com.example.fourthofficial.domain.match.MatchPhase
import com.example.fourthofficial.domain.match.MatchPlayerState
import com.example.fourthofficial.model.DiscReason
import com.example.fourthofficial.model.DiscType
import com.example.fourthofficial.model.Discipline
import com.example.fourthofficial.model.PendingSub
import com.example.fourthofficial.domain.team.Player
import com.example.fourthofficial.model.Score
import com.example.fourthofficial.model.ScoreType
import com.example.fourthofficial.model.SubBatchState
import com.example.fourthofficial.model.SubType
import com.example.fourthofficial.model.Substitution
import com.example.fourthofficial.domain.team.Team
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MatchViewModel : ViewModel() {

    //==================
    //      TEAM
    //==================

    var team1 by mutableStateOf(defaultTeam(1))
        private set

    var team2 by mutableStateOf(defaultTeam(2))
        private set

    var team1PlayerStates by mutableStateOf(defaultPlayerStates(team1))
        private set

    var team2PlayerStates by mutableStateOf(defaultPlayerStates(team2))
        private set

    fun defaultTeam(index: Int): Team = Team(
        name = "",
        index = index,
        players = List(23) { i ->
            val num = i + 1
            Player(
                number = num, name = ""
            )
        })

    private fun defaultPlayerStates(team: Team): Map<PlayerId, MatchPlayerState> {
        return team.players.mapIndexed { index, player ->
                player.id to MatchPlayerState(
                    playerId = player.id,
                    isOnField = index < 15,
                    fieldPos = if (index < 15) index + 1 else null)
            }.toMap()
    }

    fun getPlayerState(teamId: TeamId, playerId: PlayerId): MatchPlayerState? {
        val states = when (teamId) {
            team1.id -> team1PlayerStates
            team2.id -> team2PlayerStates
            else -> return null
        }
        return states[playerId]
    }

    private fun updatePlayerStates(teamId: TeamId, states: Map<PlayerId, MatchPlayerState>) {
        when (teamId) {
            team1.id -> team1PlayerStates = states
            team2.id -> team2PlayerStates = states
        }
    }

    fun updateTeam1(updated: Team) {
        team1 = updated
    }

    fun updateTeam2(updated: Team) {
        team2 = updated
    }

    fun resetPlayerStates() {
        team1PlayerStates = defaultPlayerStates(team1)
        team2PlayerStates = defaultPlayerStates(team2)
    }

    //==================
    //      SCORE
    //==================

    var scoreEvents = mutableStateListOf<Score>()
        private set

    fun recordScore(teamId: TeamId, playerId: PlayerId, scoreType: ScoreType) {
        scoreEvents.add(
            Score(
                timeMs = displayElapsedMs,
                teamId = teamId,
                halfIndex = currentHalf,
                playerId = playerId,
                type = scoreType
            )
        )
    }

    fun resetScores() {
        scoreEvents.clear()
    }

    //==================
    //      SUBS
    //==================


    var subEvents = mutableStateListOf<Substitution>()
        private set

    var subBatch by mutableStateOf<SubBatchState?>(null)
        private set

    private fun recordSub(
        teamId: TeamId, playerOffId: PlayerId, playerOnId: PlayerId, reason: SubType, time: Long, halfIndex: Int
    ) {
        subEvents.add(
            Substitution(
                timeMs = time,
                teamId = teamId,
                halfIndex = halfIndex,
                playerOffId = playerOffId,
                playerOnId = playerOnId,
                type = reason
            )
        )
    }

    fun startSubBatch(teamId: TeamId) {
        val batch = subBatch
        if (batch != null) {
            if (batch.teamId == teamId) return
            subBatch = null
        }

        subBatch = SubBatchState(
            teamId = teamId,
            timeMs = displayElapsedMs,
            halfIndex = currentHalf
        )
    }

    fun applySubBatch() {
        val batch = subBatch ?: return
        val teamStates = when (batch.teamId) {
            team1.id -> team1PlayerStates
            team2.id -> team2PlayerStates
            else -> return
        }
        var updatedStates = teamStates

        for (sub in batch.pendingSubs) {
            val playerOffState = updatedStates[sub.playerOffId] ?: return
            val playerOnState = updatedStates[sub.playerOnId] ?: return

            if (!playerOffState.isOnField) return
            if (playerOnState.isOnField) return

            val position = playerOffState.fieldPos ?: return

            updatedStates = updatedStates +
                    (sub.playerOffId to playerOffState.copy(
                        isOnField = false,
                        fieldPos = null
                    )) +
                    (sub.playerOnId to playerOnState.copy(
                        isOnField = true,
                        fieldPos = position
                    ))
        }

        updatePlayerStates(batch.teamId, updatedStates)

        for (sub in batch.pendingSubs) {
            recordSub(
                batch.teamId,
                sub.playerOffId,
                sub.playerOnId,
                sub.type,
                batch.timeMs,
                batch.halfIndex
            )
        }

        subBatch = null
    }

    fun addPendingSub(playerOffId: PlayerId, playerOnId: PlayerId, type: SubType) {
        val batch = subBatch ?: return
        if (playerOffId == playerOnId) return
        if (batch.pendingSubs.find { it.playerOffId == playerOffId } != null) return
        if (batch.pendingSubs.find { it.playerOnId == playerOnId } != null) return
        subBatch =
            batch.copy(pendingSubs = batch.pendingSubs + PendingSub(playerOffId, playerOnId, type))
    }

    fun removePendingSub(playerOffId: PlayerId) {
        val batch = subBatch ?: return
        val newPendingSubs = batch.pendingSubs.filterNot { it.playerOffId == playerOffId }
        subBatch = batch.copy(pendingSubs = newPendingSubs)
        if (newPendingSubs.isEmpty()) subBatch = null
    }

    fun getSubBatchPlayers(): List<PendingSub> {
        return subBatch?.pendingSubs ?: emptyList()
    }

    fun cancelSubBatch() {
        subBatch = null
    }

    fun resetSubs() {
        subEvents.clear()
        subBatch = null
    }

    //==================
    //      CARDS
    //==================


    var discEvents = mutableStateListOf<Discipline>()
        private set

    private val yellowDurationMs = 10L * 60L * 1000L

    fun recordDiscipline(teamId: TeamId, playerId: PlayerId, type: DiscType, reason: DiscReason) {
        val finalType = if (type == DiscType.YELLOW && discEvents.any { event ->
                event.teamId == teamId && event.playerId == playerId && event.type == DiscType.YELLOW
            }) {
            DiscType.RED
        } else type

        discEvents.add(
            Discipline(
                timeMs = displayElapsedMs,
                teamId = teamId,
                halfIndex = currentHalf,
                playerId = playerId,
                type = finalType,
                reason = reason,
            )
        )

        if (finalType == DiscType.YELLOW) applyYellow(teamId, playerId)
        else applyRed(teamId, playerId)
    }

    private fun applyYellow(teamId: TeamId, playerId: PlayerId) {
        val teamStates = when (teamId) {
            team1.id -> team1PlayerStates
            team2.id -> team2PlayerStates
            else -> return
        }

        val state = teamStates[playerId] ?: return
        val until = clock.totalElapsedMs + yellowDurationMs
        val updatedState = state.copy(yellowUntilPlayingMs = until)

        updatePlayerStates(teamId, teamStates + (playerId to updatedState))
    }

    private fun applyRed(teamId: TeamId, playerId: PlayerId) {
        val teamStates = when (teamId) {
            team1.id -> team1PlayerStates
            team2.id -> team2PlayerStates
            else -> return
        }

        val state = teamStates[playerId] ?: return
        val updatedState = state.copy(isRedCarded = true, yellowUntilPlayingMs = null)
        updatePlayerStates(teamId, teamStates + (playerId to updatedState))
    }

    fun isYellowActive(state: MatchPlayerState): Boolean {
        val until = state.yellowUntilPlayingMs ?: return false
        return clock.totalElapsedMs < until
    }

    fun yellowRemainingMs(state: MatchPlayerState): Long {
        val until = state.yellowUntilPlayingMs ?: return 0L
        return (until - clock.totalElapsedMs).coerceAtLeast(0L)
    }

    fun isRedActive(state: MatchPlayerState): Boolean {
        return state.isRedCarded
    }

    private fun clearAllCards() {
        team1PlayerStates = team1PlayerStates.mapValues { (_, state) ->
            state.copy(
                yellowUntilPlayingMs = null,
                isRedCarded = false
            )
        }

        team2PlayerStates = team2PlayerStates.mapValues { (_, state) ->
            state.copy(
                yellowUntilPlayingMs = null,
                isRedCarded = false
            )
        }
    }

    fun resetDiscs() {
        discEvents.clear()
        clearAllCards()
    }

    //==================
    //      CLOCK
    //==================


    val halfDurationMs = 40L * 60L * 1000L
    private var startRealtimeMs: Long = 0L
    private var baseHalfElapsedMs: Long = 0L
    private var baseTotalElapsedMs: Long = 0L
    private var tickerJob: Job? = null

    var clock by mutableStateOf(MatchClock())
        private set

    var phase by mutableStateOf(MatchPhase.NOT_STARTED)
        private set

    val currentHalf: Int
        get() = when (phase) {
            MatchPhase.FIRST_HALF -> 1
            MatchPhase.SECOND_HALF -> 2
            else -> 0
        }

    val halfRemainingMs: Long
        get() = (halfDurationMs - clock.halfElapsedMs).coerceAtLeast(0L)

    fun startClock() {
        if (clock.isRunning) return

        when (phase) {
            MatchPhase.NOT_STARTED -> { phase = MatchPhase.FIRST_HALF }
            MatchPhase.HALF_TIME -> { phase = MatchPhase.SECOND_HALF }
            MatchPhase.FINISHED -> return
            else -> Unit
        }

        baseHalfElapsedMs = clock.halfElapsedMs
        baseTotalElapsedMs = clock.totalElapsedMs
        startRealtimeMs = SystemClock.elapsedRealtime()

        clock = clock.copy(isRunning = true)

        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val now = SystemClock.elapsedRealtime()
                val runningMs = now - startRealtimeMs

                clock = clock.copy(
                    halfElapsedMs = baseHalfElapsedMs + runningMs,
                    totalElapsedMs = baseTotalElapsedMs + runningMs
                )

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

        clock = clock.copy(
            isRunning = false,
            halfElapsedMs = baseHalfElapsedMs + runningMs,
            totalElapsedMs = baseTotalElapsedMs + runningMs
        )
    }

    fun toggleClock() {
        if (clock.isRunning) stopClock() else startClock()
    }

    fun logHalf() {
        if (phase != MatchPhase.FIRST_HALF) return
        if (clock.halfElapsedMs < halfDurationMs) return

        stopClock()

        phase = MatchPhase.HALF_TIME

        clock = clock.copy(
            isRunning = false,
            halfElapsedMs = 0L
        )

        baseHalfElapsedMs = 0L
    }

    fun endMatch() {
        if (phase != MatchPhase.SECOND_HALF) return
        if (clock.halfElapsedMs < halfDurationMs) return

        stopClock()

        phase = MatchPhase.FINISHED
    }

    fun isClockRunning(): Boolean {
        return clock.isRunning
    }

    fun resetClock() {
        tickerJob?.cancel()
        tickerJob = null

        startRealtimeMs = 0L
        baseHalfElapsedMs = 0L
        baseTotalElapsedMs = 0L

        clock = MatchClock()
        phase = MatchPhase.NOT_STARTED
    }

    fun formatClock(ms: Long, remaining: Boolean): String {
        val remainingMs = 999 + ms
        var totalSeconds = ms / 1000
        if (remaining) {
            totalSeconds = remainingMs / 1000
        }
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    val displayElapsedMs: Long
        get() = when (phase) {
            MatchPhase.NOT_STARTED -> 0L

            MatchPhase.FIRST_HALF ->
                clock.halfElapsedMs

            MatchPhase.HALF_TIME ->
                halfDurationMs

            MatchPhase.SECOND_HALF ->
                halfDurationMs + clock.halfElapsedMs

            MatchPhase.FINISHED ->
                halfDurationMs + clock.halfElapsedMs
        }

    val totalDisplayElapsedMs: Long
        get() {
            val completedPlayingMs =
                clock.totalElapsedMs - clock.halfElapsedMs

            val completedWholeSeconds =
                (completedPlayingMs / 1000L) * 1000L

            return completedWholeSeconds + clock.halfElapsedMs
        }
}
