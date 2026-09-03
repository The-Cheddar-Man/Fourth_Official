package com.example.fourthofficial.domain.rules

import com.example.fourthofficial.domain.event.Discipline
import com.example.fourthofficial.domain.event.DisciplineType
import com.example.fourthofficial.domain.event.MatchEvent
import com.example.fourthofficial.domain.event.Score
import com.example.fourthofficial.domain.event.Substitution
import com.example.fourthofficial.domain.id.EventId
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchPlayerState

sealed interface MatchEventReplayResult {
    data class Success(
        val events: List<MatchEvent>,
        val playerStates: Map<TeamId, Map<PlayerId, MatchPlayerState>>
    ) : MatchEventReplayResult
    data class Failure(val eventId: EventId?, val message: String) : MatchEventReplayResult
}

fun replayMatchEvents(
    events: List<MatchEvent>,
    initialPlayerStates:
    Map<TeamId, Map<PlayerId, MatchPlayerState>>,
    halfDurationMs: Long,
    completedFirstHalfPlayingMs: Long?
): MatchEventReplayResult {

    val statesByTeam = initialPlayerStates.mapValues {
        (_, states) -> states.toMutableMap() }.toMutableMap()
    val normalizedEvents = events.toMutableList()
    val appliedSubstitutions = mutableListOf<Substitution>()
    val appliedDisciplines = mutableListOf<Discipline>()

    val orderedEvents =
        events
            .withIndex()
            .sortedWith(compareBy<IndexedValue<MatchEvent>> {
                it.value.halfIndex }.thenBy { it.value.timeMs }.thenBy { it.index })

    for ((originalIndex, event) in orderedEvents) {
        val playingTimeMs =
            eventPlayingTimeMs(
                event = event,
                halfDurationMs = halfDurationMs,
                completedFirstHalfPlayingMs = completedFirstHalfPlayingMs
            ) ?: return MatchEventReplayResult.Failure(
                    eventId = event.id,
                    message = "Event has an invalid time for its half."
                )

        val teamStates = statesByTeam[event.teamId] ?:
            return MatchEventReplayResult.Failure(
                eventId = event.id,
                message = "Event refers to an unknown team.")

        when (event) {
            is Score -> {
                val state = teamStates[event.playerId] ?:
                    return MatchEventReplayResult.Failure(
                        eventId = event.id,
                        message = "Score refers to a player who is not in this team.")

                if (!canActAt(state = state, playingTimeMs = playingTimeMs))
                {
                    return MatchEventReplayResult.Failure(
                        eventId = event.id,
                        message = "Scoring player was not eligible to act at this point in the match."
                    )
                }
            }

            is Substitution -> {
                if (event.playerOffId == event.playerOnId) {
                    return MatchEventReplayResult.Failure(
                        eventId = event.id,
                        message = "A player cannot substitute for themselves."
                    )
                }

                val playerOffState =
                    teamStates[event.playerOffId] ?: return MatchEventReplayResult.Failure(
                            eventId = event.id,
                            message = "Outgoing player is not in this team."
                        )

                val playerOnState =
                    teamStates[event.playerOnId] ?: return MatchEventReplayResult.Failure(
                            eventId = event.id,
                            message = "Incoming player is not in this team."
                        )

                if (!canSubstituteOff(state = playerOffState, totalElapsedMs = playingTimeMs))
                {
                    return MatchEventReplayResult.Failure(
                        eventId = event.id,
                        message = "Outgoing player was not eligible to leave the field at this point."
                    )
                }

                val playerCanReturn = canReturn(
                        events = appliedSubstitutions,
                        teamId = event.teamId,
                        playerId = event.playerOnId
                    )

                if (!canSubstituteOn(state = playerOnState,
                        totalElapsedMs = playingTimeMs, canReturn = playerCanReturn)
                ) {
                    return MatchEventReplayResult.Failure(
                        eventId = event.id,
                        message = "Incoming player was not eligible to enter the field at this point."
                    )
                }

                val fieldPosition =
                    playerOffState.fieldPos ?: return MatchEventReplayResult.Failure(
                            eventId = event.id,
                            message = "Outgoing player has no field position."
                        )

                teamStates[event.playerOffId] = playerOffState.copy(
                        isOnField = false,
                        fieldPos = null
                    )

                teamStates[event.playerOnId] = playerOnState.copy(
                        isOnField = true,
                        fieldPos = fieldPosition
                    )

                appliedSubstitutions += event
            }

            is Discipline -> {
                val state =
                    teamStates[event.playerId] ?: return MatchEventReplayResult.Failure(
                            eventId = event.id,
                            message = "Discipline event refers to a player who is not in this team."
                        )

                if (!canActAt(state = state, playingTimeMs = playingTimeMs))
                {
                    return MatchEventReplayResult.Failure(
                        eventId = event.id,
                        message = "Carded player was not eligible to act at this point in the match."
                    )
                }

                if (!isDisciplineReasonValid(event.type, event.reason))
                {
                    return MatchEventReplayResult.Failure(
                        eventId = event.id,
                        message = "Discipline reason does not match the card type."
                    )
                }

                val hasPreviousYellow = appliedDisciplines.any { previous ->
                        previous.teamId == event.teamId &&
                                previous.playerId ==
                                event.playerId &&
                                previous.type ==
                                DisciplineType.YELLOW
                    }

                val isSecondYellow = isSecondYellowCard(
                    type = event.type,
                    hasPreviousYellow = hasPreviousYellow
                )

                val normalizedEvent = event.copy(isSecondYellow = isSecondYellow)

                normalizedEvents[originalIndex] = normalizedEvent

                val updatedState =
                    when {
                        event.type == DisciplineType.RED -> { applyRedCard(state) }
                        isSecondYellow -> { applyRedCard(state) }
                        else -> { applyYellowCard(state = state, totalElapsedMs = playingTimeMs) }
                    }

                teamStates[event.playerId] = updatedState

                appliedDisciplines += normalizedEvent
            }
        }
    }

    return MatchEventReplayResult.Success(
        events = normalizedEvents,
        playerStates = statesByTeam.mapValues { (_, states) -> states.toMap() }
    )
}

private fun eventPlayingTimeMs(
    event: MatchEvent,
    halfDurationMs: Long,
    completedFirstHalfPlayingMs: Long?
): Long? {

    if (event.timeMs < 0L) {
        return null
    }

    return when (event.halfIndex) {

        1 -> {
            if (completedFirstHalfPlayingMs != null && event.timeMs > completedFirstHalfPlayingMs) {
                null
            } else {
                event.timeMs
            }
        }

        2 -> {
            val completedFirstHalf = completedFirstHalfPlayingMs ?: return null
            val secondHalfElapsedMs = event.timeMs - halfDurationMs

            if (secondHalfElapsedMs < 0L) {
                null
            } else {
                completedFirstHalf + secondHalfElapsedMs
            }
        }
        else -> null
    }
}

private fun canActAt(
    state: MatchPlayerState,
    playingTimeMs: Long
): Boolean {

    return state.isOnField && !state.isRedCarded &&
            !isYellowActive(state = state, totalElapsedMs = playingTimeMs)
}