package com.example.fourthofficial.model

data class Discipline(
    val timeMs : Long,
    val teamIndex : Int,
    val player : Int,
    val reason: String,
    val type: String
)