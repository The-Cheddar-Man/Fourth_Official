package com.example.fourthofficial.model

data class Substitution(
    val timeMs : Long,
    val teamIndex : Int,
    val halfIndex: Int,
    val playerOff : Int,
    val playerOn : Int,
    val reason: String
)