package com.example.fourthofficial.model

data class Score(
    val eventID: Int,
    val timeMs : Long,
    val teamIndex : Int,
    val halfIndex: Int,
    val player : Int,
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