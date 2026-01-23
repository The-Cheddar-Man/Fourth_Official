package com.example.fourthofficial.model

data class Substitution(
    val eventID: Int,
    val timeMs : Long,
    val teamIndex : Int,
    val halfIndex: Int,
    val playerOff : Int,
    val playerOn : Int,
    val type: SubType
)

enum class SubType(val label: String){
    TACTICAL("Tactical"),
    INJURY("Injury"), // Player cannot return
    HIA("H.I.A")
}