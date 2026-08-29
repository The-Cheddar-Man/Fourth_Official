package com.example.fourthofficial.ui.viewmodel

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.model.DiscReason
import com.example.fourthofficial.model.DiscType
import com.example.fourthofficial.model.Discipline
import com.example.fourthofficial.model.PendingSub
import com.example.fourthofficial.model.Player
import com.example.fourthofficial.model.Score
import com.example.fourthofficial.model.ScoreType
import com.example.fourthofficial.model.SubBatchState
import com.example.fourthofficial.model.SubType
import com.example.fourthofficial.model.Substitution
import com.example.fourthofficial.model.Team
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MatchClockState(
    val isRunning: Boolean = false, val elapsedMs: Long = 0L
)

class MatchViewModel : ViewModel() {

    var team1 by mutableStateOf(defaultTeam(1))
        private set

    var team2 by mutableStateOf(defaultTeam(2))
        private set

    var clock by mutableStateOf(MatchClockState())
        private set

    var scoreEvents = mutableStateListOf<Score>()
        private set

    var subEvents = mutableStateListOf<Substitution>()
        private set

    var subBatch by mutableStateOf<SubBatchState?>(null)
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
        get() = (halfDurationMs - clock.elapsedMs)

    fun defaultTeam(index: Int): Team = Team(
        name = "",
        index = index,
        players = List(23) { i ->
            val num = i + 1
            Player(
                number = num, name = "", isOnField = i < 15, fieldPos = if (i < 15) num else null
            )
        })

    fun updateTeam1(updated: Team) {
        team1 = updated
    }

    fun updateTeam2(updated: Team) {
        team2 = updated
    }

    fun recordScore(teamId: TeamId, playerId: PlayerId, scoreType: ScoreType) {
        val t = halfElapsedMs

        scoreEvents.add(
            Score(
                timeMs = t,
                teamId = teamId,
                halfIndex = if (halfTimeMs.longValue == 0L) 1 else 2,
                playerId = playerId,
                type = scoreType
            )
        )
    }

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
            timeMs = halfElapsedMs,
            halfIndex = if (halfTimeMs.longValue == 0L) 1 else 2
        )
    }

    fun applySubBatch() {
        val batch = subBatch ?: return

        val team = when (batch.teamId) {
            team1.id -> team1
            team2.id -> team2
            else -> return
        }
        val playersOff = batch.pendingSubs.map { it.playerOffId }.toSet()
        val playersOn = batch.pendingSubs.map { it.playerOnId }.toSet()

        val playerById = team.players.associateBy { it.id }

        val offPositions = batch.pendingSubs.associate { sub ->
            sub.playerOffId to playerById[sub.playerOffId]?.fieldPos
        }
        val onPositions = batch.pendingSubs.associate { it.playerOnId to it.playerOffId }

        if (playersOff.any { offPositions[it] == null }) return
        if (playersOn.any { onPositions[it] == null }) return

        val updatedPlayers = team.players.map { player ->
            when (player.id) {
                in playersOff -> {
                    player.copy(isOnField = false, fieldPos = null)
                }
                in playersOn -> {
                    val playerOffId = onPositions[player.id]!!
                    val position = offPositions[playerOffId]!!

                    player.copy(isOnField = true, fieldPos = position)
                }
                else -> player
            }
        }

        val updatedTeam = team.copy(players = updatedPlayers)
        when (batch.teamId) {
            team1.id -> team1 = updatedTeam
            team2.id -> team2 = updatedTeam
        }

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

    fun recordDiscipline(teamId: TeamId, playerId: PlayerId, type: DiscType, reason: DiscReason) {
        val t = halfElapsedMs

        val finalType = if (type == DiscType.YELLOW && discEvents.any { event ->
                event.teamId == teamId && event.playerId == playerId && event.type == DiscType.YELLOW
            }) {
            DiscType.RED
        } else type

        discEvents.add(
            Discipline(
                timeMs = t,
                teamId = teamId,
                halfIndex = if (halfTimeMs.longValue == 0L) 1 else 2,
                playerId = playerId,
                type = finalType,
                reason = reason,
            )
        )

        if (finalType == DiscType.YELLOW) applyYellow(teamId, playerId)
        else applyRed(teamId, playerId)
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
        subBatch = null
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

    private fun applyYellow(teamId: TeamId, playerId: PlayerId) {
        val team = when (teamId) {
            team1.id -> team1
            team2.id -> team2
            else -> return
        }
        val until = halfElapsedMs + yellowDurationMs

        val updatedPlayers = team.players.map { player ->
            if (player.id == playerId) {
                player.copy(yellowUntilHalfMs = until)
            }
            else {
                player
            }
        }
        val updatedTeam = team.copy(players = updatedPlayers)
        when (teamId) {
            team1.id -> team1 = updatedTeam
            team2.id -> team2 = updatedTeam
        }
    }

    private fun applyRed(teamId: TeamId, playerId: PlayerId) {
        val team = when (teamId) {
            team1.id -> team1
            team2.id -> team2
            else -> return
        }

        val updatedPlayers = team.players.map { player ->
            if (player.id == playerId) {
                player.copy(
                    isRedCarded = true,
                    yellowUntilHalfMs = null,
                )
            } else {
                player
            }
        }

        val updatedTeam = team.copy(players = updatedPlayers)
        when (teamId) {
            team1.id -> team1 = updatedTeam
            team2.id -> team2 = updatedTeam
        }
    }

    fun isYellowActive(player: Player): Boolean {
        val until = player.yellowUntilHalfMs ?: return false
        return halfElapsedMs < until
    }

    fun isRedActive(player: Player): Boolean {
        return player.isRedCarded
    }

    private fun clearAllCards() {
        team1 = team1.copy(players = team1.players.map {
            it.copy(
                yellowUntilHalfMs = null, isRedCarded = false
            )
        })
        team2 = team2.copy(players = team2.players.map {
            it.copy(
                yellowUntilHalfMs = null, isRedCarded = false
            )
        })
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
            isRunning = false, elapsedMs = baseElapsedMs + runningMs
        )
    }

    fun isClockRunning(): Boolean {
        return clock.isRunning
    }

    fun resetClock() {
        tickerJob?.cancel()
        tickerJob = null
        baseElapsedMs = 0L
        clock = MatchClockState()
        matchOffsetMs = 0L
        halfTimeMs.longValue = 0L
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
}
