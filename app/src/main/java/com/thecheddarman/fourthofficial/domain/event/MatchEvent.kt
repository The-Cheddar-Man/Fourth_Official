package com.thecheddarman.fourthofficial.domain.event

import com.thecheddarman.fourthofficial.domain.id.EventId
import com.thecheddarman.fourthofficial.domain.id.PlayerId
import com.thecheddarman.fourthofficial.domain.id.TeamId

sealed interface MatchEvent {
    val id: EventId
    val timeMs: Long
    val teamId: TeamId
    val halfIndex: Int
}

data class Score(
    override val id: EventId = EventId.new(),
    override val timeMs: Long,
    override val teamId: TeamId,
    override val halfIndex: Int,
    val playerId: PlayerId?,
    val type: ScoreType
) : MatchEvent

data class Substitution(
    override val id: EventId = EventId.new(),
    override val timeMs : Long,
    override val teamId : TeamId,
    override val halfIndex: Int,
    val playerOffId : PlayerId,
    val playerOnId : PlayerId,
    val type: SubstitutionType,
    val occurredAtHalfTime: Boolean = false
) : MatchEvent

data class Discipline(
    override val id: EventId = EventId.new(),
    override val timeMs: Long,
    override val teamId: TeamId,
    override val halfIndex: Int,
    val playerId: PlayerId,
    val type: DisciplineType,
    val reason: DisciplineReason,
    val isSecondYellow: Boolean = false
) : MatchEvent