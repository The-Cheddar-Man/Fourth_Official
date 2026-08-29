package com.example.fourthofficial.domain.match

import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.team.Team

data class MatchTeamState(
    val team: Team,
    val playerStates: Map<PlayerId, MatchPlayerState>
)