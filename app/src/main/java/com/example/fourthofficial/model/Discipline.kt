package com.example.fourthofficial.model

data class Discipline(
    val eventID: Int,
    val timeMs : Long,
    val teamIndex : Int,
    val halfIndex: Int,
    val player : Int,
    val type: DiscType,
    val reason: DiscReason
)

enum class DiscType(val label: String){
    YELLOW("Yellow Card"),
    RED("Red Card")
}


sealed interface DiscReason {
    val label: String
}

enum class DiscReasonYellow(override val label: String) : DiscReason{
    TECHNICAL("Technical"),
    FOUL_PLAY("Foul Play"),
}

enum class DiscReasonRed(override val label: String) : DiscReason{
    DANGEROUS_PLAY("Dangerous Play"),
    SERIOUS_FOUL_PLAY("Foul Play"),
    VIOLENT_CONDUCT("Violent Conduct")
}