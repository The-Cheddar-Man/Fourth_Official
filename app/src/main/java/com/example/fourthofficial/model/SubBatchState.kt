package com.example.fourthofficial.model

import com.example.fourthofficial.domain.event.SubstitutionType
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId

data class SubBatchState(
    val teamId: TeamId,
    val timeMs: Long,
    val halfIndex: Int,
    val pendingSubs: List<PendingSub> = emptyList()
)

data class PendingSub(
    val playerOffId: PlayerId,
    val playerOnId: PlayerId,
    val type: SubstitutionType
)