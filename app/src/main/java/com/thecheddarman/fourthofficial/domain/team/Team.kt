package com.thecheddarman.fourthofficial.domain.team

import com.thecheddarman.fourthofficial.domain.id.TeamId

data class Team(
    val id: TeamId = TeamId.new(),
    val name: String,
    val index: Int,
    val players: List<Player>
)