package com.example.fourthofficial.ui.viewmodel

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fourthofficial.domain.event.Discipline
import com.example.fourthofficial.domain.event.DisciplineReason
import com.example.fourthofficial.domain.event.DisciplineType
import com.example.fourthofficial.domain.event.MatchEvent
import com.example.fourthofficial.domain.event.Score
import com.example.fourthofficial.domain.event.ScoreType
import com.example.fourthofficial.domain.event.Substitution
import com.example.fourthofficial.domain.event.SubstitutionType
import com.example.fourthofficial.domain.id.EventId
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchClock
import com.example.fourthofficial.domain.match.MatchPhase
import com.example.fourthofficial.domain.match.MatchPlayerState
import com.example.fourthofficial.domain.rules.EventEditResult
import com.example.fourthofficial.domain.match.MatchState
import com.example.fourthofficial.domain.match.MatchTeamState
import com.example.fourthofficial.domain.match.PreparedSubstitution
import com.example.fourthofficial.domain.match.PreparedSubstitutionBatch
import com.example.fourthofficial.domain.rules.applyRedCard
import com.example.fourthofficial.domain.rules.applyYellowCard
import com.example.fourthofficial.domain.rules.calculateScore
import com.example.fourthofficial.domain.rules.canReturn
import com.example.fourthofficial.domain.rules.canSubstituteOff
import com.example.fourthofficial.domain.rules.canSubstituteOn
import com.example.fourthofficial.domain.rules.isDisciplineReasonValid
import com.example.fourthofficial.domain.rules.isMatchInPlay
import com.example.fourthofficial.domain.rules.isSecondYellowCard
import com.example.fourthofficial.domain.team.Player
import com.example.fourthofficial.domain.team.Team
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.fourthofficial.domain.rules.canActOnPlayer as canActOnPlayerRule
import com.example.fourthofficial.domain.rules.canFinishHalf as canFinishHalfRule
import com.example.fourthofficial.domain.rules.isYellowActive as isYellowActiveRule
import com.example.fourthofficial.domain.rules.yellowRemainingMs as yellowRemainingMsRule
import com.example.fourthofficial.domain.rules.MatchEventReplayResult
import com.example.fourthofficial.domain.rules.replayMatchEvents

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

    val preparedSubstitutionBatches: Map<TeamId, PreparedSubstitutionBatch>
        get() = matchState.preparedSubstitutionBatches

    fun getPreparedSubstitutionBatch(teamId: TeamId): PreparedSubstitutionBatch? {
        return preparedSubstitutionBatches[teamId]
    }

    val discEvents: List<Discipline>
        get() = matchState.events.filterIsInstance<Discipline>()

    private fun addEvent(event: MatchEvent) {
        matchState = matchState.copy(
            events = matchState.events + event
        )
    }

    private val completedFirstHalfPlayingMsForReplay: Long?
        get() = when (phase) {
            MatchPhase.NOT_STARTED,
            MatchPhase.FIRST_HALF -> null

            MatchPhase.HALF_TIME -> clock.totalElapsedMs

            MatchPhase.SECOND_HALF,
            MatchPhase.FINISHED -> clock.totalElapsedMs - clock.halfElapsedMs
        }

    private fun commitEventHistory(candidateEvents: List<MatchEvent>): MatchEventReplayResult {
        val result = replayMatchEvents(
            events = candidateEvents,
            initialPlayerStates = mapOf(
                team1.id to defaultPlayerStates(team1),
                team2.id to defaultPlayerStates(team2)
            ),
            halfDurationMs = halfDurationMs,
            completedFirstHalfPlayingMs = completedFirstHalfPlayingMsForReplay
            )

        if (result !is MatchEventReplayResult.Success) { return result }

        val team1States = result.playerStates[team1.id] ?: return MatchEventReplayResult.Failure(
            eventId = null,
            message = "Could not rebuild Team 1 player state."
        )

        val team2States = result.playerStates[team2.id] ?: return MatchEventReplayResult.Failure(
            eventId = null,
            message = "Could not rebuild Team 2 player state."
        )

        matchState = matchState.copy(
            events = result.events,
            team1 = matchState.team1.copy(playerStates = team1States),
            team2 = matchState.team2.copy(playerStates = team2States)
        )

        reconcilePreparedSubstitutionBatches()

        return result
    }

    private fun commitEventEdit(candidateEvents: List<MatchEvent>): EventEditResult {
        return when (val result = commitEventHistory(candidateEvents))
        {
            is MatchEventReplayResult.Success -> { EventEditResult.Success }
            is MatchEventReplayResult.Failure -> { EventEditResult.Failure(result.message) }
        }
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

        val usedPlayerOnIds = getPreparedSubstitutionBatch(teamId)
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
                alreadyUsed = player.id in usedPlayerOnIds,
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

        val usedPlayerOffIds = getPreparedSubstitutionBatch(teamId)
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
                alreadyUsed = player.id in usedPlayerOffIds
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

    fun updateScore(
        eventId: EventId,
        playerId: PlayerId,
        scoreType: ScoreType,
        timeMs: Long
    ): EventEditResult {
        val existingScore = scoreEvents.find { it.id == eventId } ?: return EventEditResult.Failure(
            "Score event could not be found.")

        val team = when (existingScore.teamId) {
            team1.id -> team1
            team2.id -> team2
            else -> return EventEditResult.Failure(
                "Score event refers to an unknown team."
            )
        }

        if (team.players.none { it.id == playerId }) { return EventEditResult.Failure(
            "Selected player is not part of this team."
        ) }
        if (timeMs < 0L) { return EventEditResult.Failure(
            "Time cannot be negative."
        ) }
        if (phase != MatchPhase.FINISHED && timeMs > displayElapsedMs) { return EventEditResult.Failure(
            "Time cannot be later than the current match clock."
        ) }

        val updatedScore = existingScore.copy(
            playerId = playerId,
            type = scoreType,
            timeMs = timeMs)

        val candidateEvents = matchState.events.map { event ->
            if (event.id == eventId) { updatedScore }
            else { event }
        }

        return commitEventEdit(candidateEvents)
    }

    fun deleteScore(eventId: EventId): EventEditResult {
        if (scoreEvents.none { it.id == eventId }) { return EventEditResult.Failure(
            "Score event could not be found."
        ) }

        val candidateEvents = matchState.events.filterNot { it.id == eventId }
        return commitEventEdit(candidateEvents)
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
        if (getPreparedSubstitutionBatch(teamId) != null) return

        matchState = matchState.copy(
            preparedSubstitutionBatches = matchState.preparedSubstitutionBatches +
                    (teamId to PreparedSubstitutionBatch(teamId = teamId))
        )
    }

    fun applyPreparedSubstitutionBatch(teamId: TeamId) {
        if (!isMatchInPlay(phase, clock)) return
        val batch = getPreparedSubstitutionBatch(teamId)  ?: return
        if (batch.substitutions.isEmpty()) return
        if (batch.substitutions.any { it.playerOnId == null || it.type == null }) return

        val teamStates = when (teamId) {
            team1.id -> team1PlayerStates
            team2.id -> team2PlayerStates
            else -> return
        }
        var updatedStates = teamStates

        for (substitution in batch.substitutions) {
            val playerOnId = substitution.playerOnId ?: return
            val playerOffState = updatedStates[substitution.playerOffId] ?: return
            val playerOnState = updatedStates[playerOnId] ?: return
            val canReturn = canReturn(
                events = subEvents, teamId = teamId, playerId = playerOnId)

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

        updatePlayerStates(teamId, updatedStates)

        for (substitution in batch.substitutions) {
            val playerOnId = substitution.playerOnId ?: return
            val type = substitution.type ?: return

            recordSubstitution(
                teamId,
                substitution.playerOffId,
                playerOnId,
                type,
                timeMs,
                halfIndex
            )
        }

        matchState = matchState.copy(
            preparedSubstitutionBatches = matchState.preparedSubstitutionBatches - teamId)
    }

    fun addPreparedSubstitution(teamId: TeamId, playerOffId: PlayerId) {
        if (!isMatchInPlay(phase, clock)) return
        val batch = getPreparedSubstitutionBatch(teamId)  ?: return
        if (batch.substitutions.any { it.playerOffId == playerOffId }) return

        val playerOffState = getPlayerState(
            teamId,
            playerOffId
        ) ?: return

        if (!canSubstituteOff(state = playerOffState, totalElapsedMs = clock.totalElapsedMs))
            return

        val preparedSubstitution = PreparedSubstitution(playerOffId = playerOffId)
        val updatedBatch = batch.copy(substitutions = batch.substitutions + preparedSubstitution)

        matchState = matchState.copy(preparedSubstitutionBatches =
            matchState.preparedSubstitutionBatches + (teamId to updatedBatch))
    }

    fun setPreparedSubstitutionPlayerOn(teamId: TeamId, playerOffId: PlayerId, playerOnId: PlayerId) {
        if (!isMatchInPlay(phase, clock)) return
        val batch = getPreparedSubstitutionBatch(teamId)  ?: return
        if (playerOffId == playerOnId) return

        val preparedSubstitution = batch.substitutions.find {
                it.playerOffId == playerOffId
            } ?: return

        val playerOnState = getPlayerState(
                teamId,
                playerOnId
            ) ?: return

        val alreadyUsed = batch.substitutions.any { it.playerOnId == playerOnId &&
            it.playerOffId != preparedSubstitution.playerOffId
        }

        val canReturn = canReturn(events = subEvents, teamId = teamId, playerId = playerOnId)

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

        val updatedBatch = batch.copy(substitutions = updatedSubstitutions)

        matchState = matchState.copy(preparedSubstitutionBatches =
            matchState.preparedSubstitutionBatches + (teamId to updatedBatch))
    }

    fun setPreparedSubstitutionType(teamId: TeamId, playerOffId: PlayerId, type: SubstitutionType) {
        if (!isMatchInPlay(phase, clock)) return
        val batch = getPreparedSubstitutionBatch(teamId) ?: return
        if (batch.substitutions.none { it.playerOffId == playerOffId }) return

        val updatedSubstitutions = batch.substitutions.map { substitution ->
            if (substitution.playerOffId == playerOffId) {
                substitution.copy(type = type)
            }
            else {
                substitution
            }
        }

        val updatedBatch = batch.copy(substitutions = updatedSubstitutions)

        matchState = matchState.copy(preparedSubstitutionBatches =
            matchState.preparedSubstitutionBatches + (teamId to updatedBatch))
    }

    fun removePreparedSubstitution(teamId: TeamId, playerOffId: PlayerId) {
        val batch = getPreparedSubstitutionBatch(teamId) ?: return
        val remainingSubstitutions = batch.substitutions.filterNot {
            it.playerOffId == playerOffId
        }

        val updatedBatch = batch.copy(substitutions = remainingSubstitutions)

        matchState = matchState.copy(preparedSubstitutionBatches =
            matchState.preparedSubstitutionBatches + (teamId to updatedBatch))
    }

    fun getPreparedSubstitutions(teamId: TeamId): List<PreparedSubstitution> {
        return getPreparedSubstitutionBatch(teamId)?.substitutions ?: emptyList()
    }

    fun cancelPreparedSubstitutionBatch(teamId: TeamId) {
        matchState = matchState.copy(
            preparedSubstitutionBatches = matchState.preparedSubstitutionBatches - teamId)
    }

    private fun reconcilePreparedSubstitutionBatches() {
        if (matchState.preparedSubstitutionBatches.isEmpty()) { return }

        val reconciledBatches = matchState.preparedSubstitutionBatches.mapNotNull { (teamId, batch) ->
            val teamStates = when (teamId) {
                    team1.id -> team1PlayerStates
                    team2.id -> team2PlayerStates
                    else -> return@mapNotNull null
                }

            val validOutgoingSubstitutions = batch.substitutions.filter { substitution ->
                    val playerOffState = teamStates[substitution.playerOffId] ?: return@filter false
                    canSubstituteOff(
                        state = playerOffState,
                        totalElapsedMs = clock.totalElapsedMs
                    )
                }

            val usedPlayerOnIds = mutableSetOf<PlayerId>()
            val reconciledSubstitutions = validOutgoingSubstitutions.map { substitution ->
                    val playerOnId = substitution.playerOnId ?: return@map substitution
                    if (playerOnId == substitution.playerOffId) {
                        return@map substitution.copy(playerOnId = null)
                    }

                    val playerOnState = teamStates[playerOnId] ?: return@map substitution.copy(playerOnId = null)

                    val playerCanReturn = canReturn(
                        events = subEvents,
                        teamId = teamId,
                        playerId = playerOnId
                    )

                    val incomingIsValid = canSubstituteOn(
                        state = playerOnState,
                        totalElapsedMs = clock.totalElapsedMs,
                        alreadyUsed = playerOnId in usedPlayerOnIds,
                        canReturn = playerCanReturn
                    )

                    if (incomingIsValid) {
                        usedPlayerOnIds += playerOnId
                        substitution
                    } else {
                        substitution.copy(playerOnId = null)
                    }
                }

            if (reconciledSubstitutions.isEmpty()) {
                null
            } else {
                teamId to batch.copy(substitutions = reconciledSubstitutions)
            }
        }.toMap()

        matchState = matchState.copy(preparedSubstitutionBatches = reconciledBatches)
    }

    fun resetSubstitutions() {
        matchState = matchState.copy(
            events = matchState.events.filterNot { it is Substitution },
            preparedSubstitutionBatches = emptyMap()
        )
    }

    fun updateSubstitution(eventId: EventId, playerOffId: PlayerId,
                           playerOnId: PlayerId, type: SubstitutionType, timeMs: Long): EventEditResult
    {
        val existingSubstitution = subEvents.find { event -> event.id == eventId } ?: return EventEditResult.Failure(
            "Substitution event could not be found."
        )

        val team = when (existingSubstitution.teamId) {
                team1.id -> team1
                team2.id -> team2
                else -> return EventEditResult.Failure(
                    "Substitution refers to an unknown team."
                )
            }

        if (team.players.none { it.id == playerOffId })
        {
            return EventEditResult.Failure(
                "Outgoing player is not part of this team."
            )
        }

        if (team.players.none { it.id == playerOnId })
        {
            return EventEditResult.Failure(
                "Incoming player is not part of this team."
            )
        }

        if (playerOffId == playerOnId) { return EventEditResult.Failure(
            "A player cannot substitute for themselves."
        ) }
        if (timeMs < 0L) { return EventEditResult.Failure(
            "Time cannot be negative."
        ) }
        if (phase != MatchPhase.FINISHED && timeMs > displayElapsedMs) { return EventEditResult.Failure(
            "Time cannot be later than the current match clock."
        ) }

        val updatedSubstitution = existingSubstitution.copy(
            playerOffId = playerOffId,
            playerOnId = playerOnId,
            type = type,
            timeMs = timeMs
        )

        val candidateEvents = matchState.events.map { event ->
            if (event.id == eventId) {
                updatedSubstitution
            } else {
                event
            }
        }

        return commitEventEdit(candidateEvents)
    }

    fun deleteSubstitution(eventId: EventId): EventEditResult
    {
        if (subEvents.none { event -> event.id == eventId }) { return EventEditResult.Failure(
            "Substitution event could not be found."
        ) }
        val candidateEvents = matchState.events.filterNot { event -> event.id == eventId }

        return commitEventEdit(candidateEvents)
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

        val eventPlayingTimeMs = clock.totalElapsedMs - (displayElapsedMs - eventTimeMs)

        when {
            type == DisciplineType.RED -> applyRed(teamId, playerId)
            isSecondYellow -> applyRed(teamId, playerId)
            else -> applyYellow(teamId, playerId, eventPlayingTimeMs)
        }

        reconcilePreparedSubstitutionBatches()
    }

    fun updateDiscipline(eventId: EventId, playerId: PlayerId,
        type: DisciplineType, reason: DisciplineReason, timeMs: Long): EventEditResult  {

        val existingDiscipline = discEvents.find { event -> event.id == eventId } ?: return EventEditResult.Failure(
            "Discipline event could not be found."
        )
        val team = when (existingDiscipline.teamId) {
                team1.id -> team1
                team2.id -> team2
                else -> return EventEditResult.Failure(
                    "Discipline event refers to an unknown team."
                )
            }

        if (team.players.none { it.id == playerId }) { return EventEditResult.Failure(
            "Selected player is not part of this team."
        ) }
        if (!isDisciplineReasonValid(type = type, reason = reason)) { return EventEditResult.Failure(
            "This reason is not valid for the selected card type."
        ) }
        if (timeMs < 0L) { return EventEditResult.Failure(
            "Time cannot be negative."
        ) }
        if (phase != MatchPhase.FINISHED && timeMs > displayElapsedMs) { return EventEditResult.Failure(
            "Time cannot be later than the current match clock."
        ) }

        val updatedDiscipline = existingDiscipline.copy(
                playerId = playerId,
                type = type,
                reason = reason,
                timeMs = timeMs,
                isSecondYellow = false
            )

        val candidateEvents = matchState.events.map { event ->
                if (event.id == eventId) {
                    updatedDiscipline
                } else {
                    event
                }
            }

        return commitEventEdit(candidateEvents)
    }

    fun deleteDiscipline(eventId: EventId): EventEditResult  {
        if (discEvents.none { event -> event.id == eventId }) { return EventEditResult.Failure(
            "Discipline event could not be found."
        ) }

        val candidateEvents = matchState.events.filterNot { event -> event.id == eventId }
        return commitEventEdit(candidateEvents)
    }

    private fun applyYellow(teamId: TeamId, playerId: PlayerId, eventPlayingTimeMs: Long) {
        val teamStates = when (teamId) {
            team1.id -> team1PlayerStates
            team2.id -> team2PlayerStates
            else -> return
        }

        val state = teamStates[playerId] ?: return
        val updatedState = applyYellowCard(state = state, totalElapsedMs = eventPlayingTimeMs)

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
