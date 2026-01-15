package com.example.fourthofficial.model

data class Score(
    val timeMs : Long,
    val teamIndex : Int,
    val halfIndex: Int,
    val player : Int,
    val type: String
)