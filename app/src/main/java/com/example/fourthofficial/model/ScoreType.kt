package com.example.fourthofficial.model

enum class ScoreType(val points: Int, val label: String) {
    TRY(5, "Try"),
    CONVERSION(2, "Conversion"),
    PENALTY(3, "Penalty"),
    DROP_GOAL(3, "Drop goal")
}