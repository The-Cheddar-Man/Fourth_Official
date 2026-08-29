package com.example.fourthofficial.model

import com.example.fourthofficial.domain.id.EventId
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId

data class Substitution(
    val id: EventId = EventId.new(),
    val timeMs : Long,
    val teamId : TeamId,
    val halfIndex: Int,
    val playerOffId : PlayerId,
    val playerOnId : PlayerId,
    val type: SubType
)

data class SubBatchState(
    val teamId: TeamId,
    val timeMs: Long,
    val halfIndex: Int,
    val pendingSubs: List<PendingSub> = emptyList()
)

data class PendingSub(
    val playerOffId: PlayerId,
    val playerOnId: PlayerId,
    val type: SubType
)

enum class SubType(val label: String){
    TACTICAL("Tactical"),
    INJURY("Injury"), // Player cannot return
    HIA("H.I.A")
}