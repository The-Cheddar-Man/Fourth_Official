package com.example.fourthofficial.model

import com.example.fourthofficial.domain.id.EventId
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId

data class Score(
    val id: EventId = EventId.new(),
    val timeMs: Long,
    val teamId: TeamId,
    val halfIndex: Int,
    val playerId: PlayerId,
    val type: ScoreType
)

enum class ScoreType(val label: String){
    TRY("Try"),
    CONVERSION_MADE("Conversion Made"),
    CONVERSION_MISSED("Conversion Missed"),
    PENALTY_MADE("Penalty Made"),
    PENALTY_MISSED("Penalty Missed"),
    PENALTY_TRY("Penalty Try"),
    DROP_GOAL_MADE("Drop Goal Made"),
    DROP_GOAL_MISSED("Drop Goal Missed"),
}

fun GetScoreTypePoints(type: ScoreType): Int = when (type) {
    ScoreType.TRY -> 5
    ScoreType.CONVERSION_MADE -> 2
    ScoreType.PENALTY_MADE -> 3
    ScoreType.PENALTY_TRY -> 5
    ScoreType.DROP_GOAL_MADE -> 3
    else -> 0
}