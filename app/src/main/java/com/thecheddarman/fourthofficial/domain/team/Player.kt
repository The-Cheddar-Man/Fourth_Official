package com.thecheddarman.fourthofficial.domain.team

import com.thecheddarman.fourthofficial.domain.id.PlayerId

data class Player(
    val id: PlayerId = PlayerId.new(),
    val name: String,
    val number: Int,
)