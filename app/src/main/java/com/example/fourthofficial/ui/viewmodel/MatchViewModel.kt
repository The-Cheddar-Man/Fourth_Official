package com.example.fourthofficial.ui.viewmodel

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchClock
import com.example.fourthofficial.domain.event.MatchEvent
import com.example.fourthofficial.domain.match.MatchPhase
import com.example.fourthofficial.domain.match.MatchPlayerState
import com.example.fourthofficial.domain.match.MatchState
import com.example.fourthofficial.domain.match.MatchTeamState
import com.example.fourthofficial.domain.event.DisciplineType
import com.example.fourthofficial.domain.event.Discipline
import com.example.fourthofficial.domain.event.DisciplineReason
import com.example.fourthofficial.model.PendingSub
import com.example.fourthofficial.domain.team.Player
import com.example.fourthofficial.domain.event.Score
import com.example.fourthofficial.domain.event.ScoreType
import com.example.fourthofficial.model.SubBatchState
import com.example.fourthofficial.domain.event.SubstitutionType
import com.example.fourthofficial.domain.event.Substitution
import com.example.fourthofficial.domain.team.Team
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MatchViewModel : ViewModel() {

    //==================
    //    Interface
    //==================

    val team1: Team
        get() = matchState.team1.team

    val team2: Team
        get() = matchState.team2.team

    val team1PlayerStates: Map<PlayerId, MatchPlayerState>
        get() = matchState.team1.playerStates

    val team2PlayerStates: Map<PlayerId, MatchPlayerState>
        get() = matchState.team2.playerStates

    val clock: MatchClock
        get() = matchState.clock

    val phase: MatchPhase
        get() = matchState.phase

    val scoreEvents: List<Score>
        get() = matchState.events.filterIsInstance<Score>()

    val subEvents: List<Substitution>
        get() = matchState.events.filterIsInstance<Substitution>()

    val discEvents: List<Discipline>
        get() = matchState.events.filterIsInstance<Discipline>()

    private fun addEvent(event: MatchEvent) {
        matchState = matchState.copy(
            events = matchState.events + event
        )
    }

    //==================
    //      TEAM
    //==================

    var matchState by mutableStateOf(defaultMatchState())
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

    private fun defaultMatchTeamState(team: Team): MatchTeamState {
        return MatchTeamState(
            team = team,
            playerStates = defaultPlayerStates(team)
        )
    }

    private fun defaultMatchState(): MatchState {
        val team1 = defaultTeam(1)
        val team2 = defaultTeam(2)

        return MatchState(
            team1 = defaultMatchTeamState(team1),
            team2 = defaultMatchTeamState(team2)
        )
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
        matchState = when (teamId) {
            team1.id -> matchState.copy(team1 = matchState.team1.copy(playerStates = states))
            team2.id -> matchState.copy(team2 = matchState.team2.copy(playerStates = states))
            else -> matchState
        }
    }

    fun updateTeam1(updated: Team) {
        matchState = matchState.copy(
            team1 = matchState.team1.copy(
                team = updated
            )
        )
    }

    fun updateTeam2(updated: Team) {
        matchState = matchState.copy(
            team2 = matchState.team2.copy(
                team = updated
            )
        )
    }

    fun resetPlayerStates() {
        matchState = matchState.copy(
            team1 = matchState.team1.copy(playerStates = defaultPlayerStates(team1)),
            team2 = matchState.team2.copy(playerStates = defaultPlayerStates(team2))
        )
    }

    //==================
    //      SCORE
    //==================

    fun recordScore(teamId: TeamId, playerId: PlayerId, scoreType: ScoreType) {
        val score = Score(
            timeMs = displayElapsedMs,
            teamId = teamId,
            halfIndex = currentHalf,
            playerId = playerId,
            type = scoreType
        )

        addEvent(score)
    }

    fun resetScores() {
        matchState = matchState.copy(events = matchState.events.filterNot { it is Score })
    }

    //==================
    //      SUBS
    //==================

    var subBatch by mutableStateOf<SubBatchState?>(null)
        private set

    private fun recordSub(teamId: TeamId, playerOffId: PlayerId, playerOnId: PlayerId,
                          reason: SubstitutionType, time: Long, halfIndex: Int) {
        val subs = Substitution(
            timeMs = time,
            teamId = teamId,
            halfIndex = halfIndex,
            playerOffId = playerOffId,
            playerOnId = playerOnId,
            type = reason
        )
        addEvent(subs)
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

    fun addPendingSub(playerOffId: PlayerId, playerOnId: PlayerId, type: SubstitutionType) {
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
        matchState = matchState.copy(events = matchState.events.filterNot { it is Substitution })
        subBatch = null
    }

    //==================
    //      CARDS
    //==================

    private val yellowDurationMs = 10L * 60L * 1000L

    fun recordDiscipline(teamId: TeamId, playerId: PlayerId, type: DisciplineType, reason: DisciplineReason) {
        val finalType = if (type == DisciplineType.YELLOW && discEvents.any { event ->
                event.teamId == teamId && event.playerId == playerId && event.type == DisciplineType.YELLOW
            }) {
            DisciplineType.RED
        } else type

        val discs = Discipline(
            timeMs = displayElapsedMs,
            teamId = teamId,
            halfIndex = currentHalf,
            playerId = playerId,
            type = finalType,
            reason = reason,
        )
        addEvent(discs)

        if (finalType == DisciplineType.YELLOW) applyYellow(teamId, playerId)
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
        matchState = matchState.copy(
            team1 = matchState.team1.copy(
                playerStates = team1PlayerStates.mapValues { (_, state) ->
                    state.copy(
                        yellowUntilPlayingMs = null,
                        isRedCarded = false
                    ) }),
            team2 = matchState.team2.copy(
                playerStates = team2PlayerStates.mapValues { (_, state) ->
                    state.copy(
                        yellowUntilPlayingMs = null,
                        isRedCarded = false
                    ) })
        )
    }

    fun resetDiscs() {
        matchState = matchState.copy(events = matchState.events.filterNot { it is Discipline })
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

        val newPhase = when (phase) {
            MatchPhase.NOT_STARTED -> MatchPhase.FIRST_HALF
            MatchPhase.HALF_TIME -> MatchPhase.SECOND_HALF
            MatchPhase.FINISHED -> return
            else -> phase
        }

        baseHalfElapsedMs = clock.halfElapsedMs
        baseTotalElapsedMs = clock.totalElapsedMs
        startRealtimeMs = SystemClock.elapsedRealtime()

        matchState = matchState.copy(
            phase = newPhase, clock = matchState.clock.copy(isRunning = true))

        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val now = SystemClock.elapsedRealtime()
                val runningMs = now - startRealtimeMs

                matchState = matchState.copy(
                    clock = matchState.clock.copy(
                        halfElapsedMs = baseHalfElapsedMs + runningMs,
                        totalElapsedMs = baseTotalElapsedMs + runningMs
                    )
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

        matchState = matchState.copy(
            clock = matchState.clock.copy(
                isRunning = false,
                halfElapsedMs = baseHalfElapsedMs + runningMs,
                totalElapsedMs = baseTotalElapsedMs + runningMs
            )
        )
    }

    fun toggleClock() {
        if (clock.isRunning) stopClock() else startClock()
    }

    fun logHalf() {
        if (phase != MatchPhase.FIRST_HALF) return
        if (clock.halfElapsedMs < halfDurationMs) return

        stopClock()

        matchState = matchState.copy(
            phase = MatchPhase.HALF_TIME,
            clock = matchState.clock.copy(isRunning = false, halfElapsedMs = 0L)
        )

        baseHalfElapsedMs = 0L
    }

    fun endMatch() {
        if (phase != MatchPhase.SECOND_HALF) return
        if (clock.halfElapsedMs < halfDurationMs) return

        stopClock()

        matchState = matchState.copy(
            phase = MatchPhase.FINISHED
        )
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

        matchState = matchState.copy(
            phase = MatchPhase.NOT_STARTED,
            clock = MatchClock()
        )
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
