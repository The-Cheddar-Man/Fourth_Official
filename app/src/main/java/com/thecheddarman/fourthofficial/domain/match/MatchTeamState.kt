package com.thecheddarman.fourthofficial.domain.match

import com.thecheddarman.fourthofficial.domain.id.PlayerId
import com.thecheddarman.fourthofficial.domain.team.Team

data class MatchTeamState(
    val team: Team,
    val playerStates: Map<PlayerId, MatchPlayerState>
)