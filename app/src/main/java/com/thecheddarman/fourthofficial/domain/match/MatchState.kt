package com.thecheddarman.fourthofficial.domain.match

import com.thecheddarman.fourthofficial.domain.event.MatchEvent
import com.thecheddarman.fourthofficial.domain.id.MatchId
import com.thecheddarman.fourthofficial.domain.id.TeamId

data class MatchState(
    val id: MatchId = MatchId.new(),

    val phase: MatchPhase = MatchPhase.NOT_STARTED,
    val clock: MatchClock = MatchClock(),

    val team1: MatchTeamState,
    val team2: MatchTeamState,

    val events: List<MatchEvent> = emptyList(),
    val preparedSubstitutionBatches: Map<TeamId, PreparedSubstitutionBatch> = emptyMap()
)