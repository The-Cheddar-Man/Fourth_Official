package com.example.fourthofficial.domain.team

import com.example.fourthofficial.domain.id.TeamId

data class Team(
    val id: TeamId = TeamId.new(),
    val name: String,
    val index: Int,
    val players: List<Player>
)