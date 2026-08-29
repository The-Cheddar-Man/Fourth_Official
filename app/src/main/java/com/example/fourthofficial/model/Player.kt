package com.example.fourthofficial.model

import com.example.fourthofficial.domain.id.PlayerId

data class Player(
    val id: PlayerId = PlayerId.new(),
    val name: String,
    val number: Int,
)