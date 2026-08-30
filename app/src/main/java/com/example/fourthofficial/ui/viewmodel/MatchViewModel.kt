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
import com.example.fourthofficial.domain.team.Player
import com.example.fourthofficial.domain.event.Score
import com.example.fourthofficial.domain.event.ScoreType
import com.example.fourthofficial.domain.event.SubstitutionType
import com.example.fourthofficial.domain.event.Substitution
import com.example.fourthofficial.domain.match.PreparedSubstitution
import com.example.fourthofficial.domain.match.PreparedSubstitutionBatch
import com.example.fourthofficial.domain.rules.applyRedCard
import com.example.fourthofficial.domain.rules.applyYellowCard
import com.example.fourthofficial.domain.team.Team
import com.example.fourthofficial.domain.rules.isYellowActive as isYellowActiveRule
import com.example.fourthofficial.domain.rules.isDisciplineReasonValid
import com.example.fourthofficial.domain.rules.canActOnPlayer as canActOnPlayerRule
import com.example.fourthofficial.domain.rules.canFinishHalf as canFinishHalfRule
import com.example.fourthofficial.domain.rules.yellowRemainingMs as yellowRemainingMsRule
import com.example.fourthofficial.domain.rules.canSubstituteOff
import com.example.fourthofficial.domain.rules.canSubstituteOn
import com.example.fourthofficial.domain.rules.isMatchInPlay
import com.example.fourthofficial.domain.rules.calculateScore
import com.example.fourthofficial.domain.rules.isSecondYellowCard
import com.example.fourthofficial.domain.rules.canReturn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MatchViewModel : ViewModel() {

    //==================
    //    Interface
    //==================

    //region Interface
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

    val preparedSubstitutionBatch: PreparedSubstitutionBatch?
        get() = matchState.preparedSubstitutionBatch

    val discEvents: List<Discipline>
        get() = matchState.events.filterIsInstance<Discipline>()

    private fun addEvent(event: MatchEvent) {
        matchState = matchState.copy(
            events = matchState.events + event
        )
    }

    fun canActOnPlayer(state: MatchPlayerState): Boolean {
        return canActOnPlayerRule(state = state, phase = phase, clock = clock)
    }

    fun eligiblePlayersOn(teamId: TeamId): List<Player> {
        if (!isMatchInPlay(phase, clock)) return emptyList()

        val team = when (teamId) {
            team1.id -> team1
            team2.id -> team2
            else -> return emptyList()
        }

        val usedOn = preparedSubstitutionBatch
            ?.takeIf { it.teamId == teamId }
            ?.substitutions
            ?.mapNotNull { it.playerOnId }
            ?.toSet()
            ?: emptySet()

        return team.players.filter { player ->
            val state = getPlayerState(teamId, player.id) ?: return@filter false
            val canReturn = canReturn(events = subEvents, teamId = teamId, playerId = player.id)
            canSubstituteOn(
                state = state,
                totalElapsedMs = clock.totalElapsedMs,
                alreadyUsed = player.id in usedOn,
                canReturn = canReturn
            )
        }
    }

    fun eligiblePlayersOff(teamId: TeamId): List<Player> {
        if (!isMatchInPlay(phase, clock)) return emptyList()

        val team = when (teamId) {
            team1.id -> team1
            team2.id -> team2
            else -> return emptyList()
        }

        val usedOff = preparedSubstitutionBatch
            ?.takeIf { it.teamId == teamId }
            ?.substitutions
            ?.map { it.playerOffId }
            ?.toSet()
            ?: emptySet()

        return team.players.filter { player ->
            val state = getPlayerState(teamId, player.id)
                ?: return@filter false

            canSubstituteOff(
                state = state,
                totalElapsedMs = clock.totalElapsedMs,
                alreadyUsed = player.id in usedOff
            )
        }
    }

    val canFinishHalf: Boolean
        get() = canFinishHalfRule(
            phase = phase,
            halfElapsedMs = clock.halfElapsedMs,
            halfDurationMs = halfDurationMs
        )

    fun scoreForTeam(teamId: TeamId): Int {
        return calculateScore(
            scoreEvents.filter { it.teamId == teamId }
        )
    }

    fun halfTimeScoreForTeam(teamId: TeamId): Int {
        return calculateScore(
            scoreEvents.filter {
                it.teamId == teamId && it.halfIndex == 1
            }
        )
    }

    fun canAddAnotherSubstitution(teamId: TeamId): Boolean {
        return eligiblePlayersOff(teamId).isNotEmpty() && eligiblePlayersOn(teamId).isNotEmpty()
    }
    //endregion

    //==================
    //      TEAM
    //==================

    //region Team
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
    //endregion

    //==================
    //      SCORE
    //==================

    //region Score
    fun recordScore(teamId: TeamId, playerId: PlayerId, scoreType: ScoreType,
                    eventTimeMs: Long, halfIndex: Int) {
        val playerState = getPlayerState(teamId, playerId) ?: return
        if (!canActOnPlayer(playerState)) return

        val score = Score(
            timeMs = eventTimeMs,
            teamId = teamId,
            halfIndex = halfIndex,
            playerId = playerId,
            type = scoreType
        )

        addEvent(score)
    }

    fun resetScores() {
        matchState = matchState.copy(events = matchState.events.filterNot { it is Score })
    }
    //endregion

    //==================
    //      SUBS
    //==================

    //region Subs

    private fun recordSubstitution(teamId: TeamId, playerOffId: PlayerId, playerOnId: PlayerId,
                          reason: SubstitutionType, time: Long, halfIndex: Int) {
        val substitution = Substitution(
            timeMs = time,
            teamId = teamId,
            halfIndex = halfIndex,
            playerOffId = playerOffId,
            playerOnId = playerOnId,
            type = reason
        )
        addEvent(substitution)
    }

    fun startPreparedSubstitutionBatch(teamId: TeamId) {
        val existingBatch = preparedSubstitutionBatch

        if (existingBatch != null) {
            if (existingBatch.teamId == teamId)
                return
        }

        matchState = matchState.copy(
            preparedSubstitutionBatch = PreparedSubstitutionBatch(teamId = teamId)
        )
    }

    fun applyPreparedSubstitutionBatch() {
        if (!isMatchInPlay(phase, clock)) return
        val batch = preparedSubstitutionBatch  ?: return
        if (batch.substitutions.isEmpty()) return
        if (batch.substitutions.any { it.playerOnId == null || it.type == null }) return

        val teamStates = when (batch.teamId) {
            team1.id -> team1PlayerStates
            team2.id -> team2PlayerStates
            else -> return
        }
        var updatedStates = teamStates

        for (substitution in batch.substitutions) {
            val playerOnId = substitution.playerOnId ?: return
            if (substitution.type == null) return
            val playerOffState = updatedStates[substitution.playerOffId] ?: return
            val playerOnState = updatedStates[substitution.playerOnId] ?: return
            val canReturn = canReturn(
                events = subEvents, teamId = batch.teamId, playerId = playerOnId)

            if (!canSubstituteOff(
                    state = playerOffState,
                    totalElapsedMs = clock.totalElapsedMs))
                return

            if (!canSubstituteOn(
                    state = playerOnState,
                    totalElapsedMs = clock.totalElapsedMs,
                    canReturn = canReturn))
                return

            val position = playerOffState.fieldPos ?: return

            updatedStates = updatedStates +
                (substitution.playerOffId to playerOffState.copy(
                    isOnField = false,
                    fieldPos = null
                )) +
                (playerOnId to playerOnState.copy(
                    isOnField = true,
                    fieldPos = position
                ))
        }

        val timeMs = displayElapsedMs
        val halfIndex = currentHalf

        updatePlayerStates(batch.teamId, updatedStates)

        for (substitution in batch.substitutions) {
            val playerOnId = substitution.playerOnId ?: return
            val type = substitution.type ?: return

            recordSubstitution(
                batch.teamId,
                substitution.playerOffId,
                playerOnId,
                type,
                timeMs,
                halfIndex
            )
        }

        matchState = matchState.copy(preparedSubstitutionBatch = null)
    }

    fun addPreparedSubstitution(playerOffId: PlayerId) {
        if (!isMatchInPlay(phase, clock)) return
        val batch = preparedSubstitutionBatch ?: return
        if (batch.substitutions.any { it.playerOffId == playerOffId }) return

        val playerOffState = getPlayerState(
                batch.teamId,
                playerOffId
            ) ?: return

        if (!canSubstituteOff(state = playerOffState, totalElapsedMs = clock.totalElapsedMs))
            return

        val preparedSubstitution = PreparedSubstitution(playerOffId = playerOffId)

        matchState = matchState.copy(preparedSubstitutionBatch = batch.copy(
            substitutions = batch.substitutions + preparedSubstitution))
    }

    fun setPreparedSubstitutionPlayerOn(playerOffId: PlayerId, playerOnId: PlayerId) {
        if (!isMatchInPlay(phase, clock)) return
        val batch = preparedSubstitutionBatch ?: return
        if (playerOffId == playerOnId) return

        val preparedSubstitution = batch.substitutions.find {
                it.playerOffId == playerOffId
            } ?: return

        val playerOnState = getPlayerState(
                batch.teamId,
                playerOnId
            ) ?: return

        val alreadyUsed = batch.substitutions.any { it.playerOnId == playerOnId &&
            it.playerOffId != preparedSubstitution.playerOffId
        }

        val canReturn = canReturn(events = subEvents, teamId = batch.teamId, playerId = playerOnId)

        if (!canSubstituteOn(
                state = playerOnState,
                totalElapsedMs = clock.totalElapsedMs,
                alreadyUsed = alreadyUsed,
                canReturn = canReturn
            )
        ) return

        val updatedSubstitutions = batch.substitutions.map { substitution ->
                if (substitution.playerOffId == playerOffId) {
                    substitution.copy(playerOnId = playerOnId)
                }
                else {
                    substitution
                }
            }

        matchState = matchState.copy(
            preparedSubstitutionBatch = batch.copy(substitutions = updatedSubstitutions))
    }

    fun setPreparedSubstitutionType(playerOffId: PlayerId, type: SubstitutionType) {
        if (!isMatchInPlay(phase, clock)) return
        val batch = preparedSubstitutionBatch ?: return
        if (batch.substitutions.none { it.playerOffId == playerOffId }) return

        val updatedSubstitutions = batch.substitutions.map { substitution ->
                if (substitution.playerOffId == playerOffId) {
                    substitution.copy(type = type)
                }
                else {
                    substitution
                }
            }

        matchState = matchState.copy(
            preparedSubstitutionBatch = batch.copy(substitutions = updatedSubstitutions))
    }

    fun removePreparedSubstitution(playerOffId: PlayerId) {
        val batch = preparedSubstitutionBatch ?: return
        val remaining = batch.substitutions.filterNot {
            it.playerOffId == playerOffId
        }

        matchState = matchState.copy(preparedSubstitutionBatch =
            if (remaining.isEmpty())
                null
            else
                batch.copy(substitutions = remaining)
        )
    }

    fun getPreparedSubstitutions(): List<PreparedSubstitution> {
        return preparedSubstitutionBatch?.substitutions ?: emptyList()
    }

    fun cancelPreparedSubstitutionBatch() {
        matchState = matchState.copy(preparedSubstitutionBatch = null)
    }

    fun resetSubstitutions() {
        matchState = matchState.copy(
            events = matchState.events.filterNot { it is Substitution },
            preparedSubstitutionBatch = null
        )
    }
    //endregion

    //==================
    //   DISCIPLINES
    //==================

    //region Disciplines
    fun recordDiscipline(teamId: TeamId, playerId: PlayerId, type: DisciplineType,
                         reason: DisciplineReason, eventTimeMs: Long, halfIndex: Int) {
        val playerState = getPlayerState(teamId, playerId) ?: return
        if (!canActOnPlayer(playerState)) return
        if (!isDisciplineReasonValid(type, reason)) return

        val hasPreviousYellow = discEvents.any {
            it.teamId == teamId && it.playerId == playerId && it.type == DisciplineType.YELLOW
        }
        val isSecondYellow = isSecondYellowCard(type = type, hasPreviousYellow = hasPreviousYellow)

        val discs = Discipline(
            timeMs = eventTimeMs,
            teamId = teamId,
            halfIndex = halfIndex,
            playerId = playerId,
            type = type,
            reason = reason,
            isSecondYellow = isSecondYellow
        )
        addEvent(discs)

        when {
            type == DisciplineType.RED -> applyRed(teamId, playerId)
            isSecondYellow -> applyRed(teamId, playerId)
            else -> applyYellow(teamId, playerId)
        }
    }

    private fun applyYellow(teamId: TeamId, playerId: PlayerId) {
        val teamStates = when (teamId) {
            team1.id -> team1PlayerStates
            team2.id -> team2PlayerStates
            else -> return
        }

        val state = teamStates[playerId] ?: return
        val updatedState = applyYellowCard(state = state, totalElapsedMs = clock.totalElapsedMs)

        updatePlayerStates(teamId, teamStates + (playerId to updatedState))
    }

    private fun applyRed(teamId: TeamId, playerId: PlayerId) {
        val teamStates = when (teamId) {
            team1.id -> team1PlayerStates
            team2.id -> team2PlayerStates
            else -> return
        }

        val state = teamStates[playerId] ?: return
        val updatedState = applyRedCard(state)
        updatePlayerStates(teamId, teamStates + (playerId to updatedState))
    }

    fun isYellowActive(state: MatchPlayerState): Boolean {
        return isYellowActiveRule(
            state = state,
            totalElapsedMs = clock.totalElapsedMs
        )
    }

    fun yellowRemainingMs(state: MatchPlayerState): Long {
        return yellowRemainingMsRule(state, clock.totalElapsedMs)
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
    //endregion

    //==================
    //      CLOCK
    //==================

    //region Clock
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
        if (!canFinishHalf) return

        stopClock()

        matchState = matchState.copy(
            phase = MatchPhase.HALF_TIME,
            clock = matchState.clock.copy(isRunning = false, halfElapsedMs = 0L)
        )

        baseHalfElapsedMs = 0L
    }

    fun endMatch() {
        if (phase != MatchPhase.SECOND_HALF) return
        if (!canFinishHalf) return

        stopClock()

        matchState = matchState.copy(
            phase = MatchPhase.FINISHED
        )
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
    //endregion
}
