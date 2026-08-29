package com.example.fourthofficial.model

import com.example.fourthofficial.domain.id.EventId
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId

data class Discipline(
    val id: EventId = EventId.new(),
    val timeMs: Long,
    val teamId: TeamId,
    val halfIndex: Int,
    val playerId: PlayerId,
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