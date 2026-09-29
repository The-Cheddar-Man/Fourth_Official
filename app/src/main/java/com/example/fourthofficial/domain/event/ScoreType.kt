package com.example.fourthofficial.domain.event

enum class ScoreType(val label: String, val requiresPlayer: Boolean = true){
    TRY("Try"),
    CONVERSION_MADE("Conversion Made"),
    CONVERSION_MISSED("Conversion Missed"),
    PENALTY_MADE("Penalty Made"),
    PENALTY_MISSED("Penalty Missed"),
    PENALTY_TRY(label = "Penalty Try", requiresPlayer = false),
    DROP_GOAL_MADE("Drop Goal Made"),
    DROP_GOAL_MISSED("Drop Goal Missed"),
}