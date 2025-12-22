package com.example.fourthofficial.model

import com.example.fourthofficial.model.ScoreType

data class Score(
    val timeMs : Long,
    val teamIndex : Int,
    val player : Int,
    val type: ScoreType
)