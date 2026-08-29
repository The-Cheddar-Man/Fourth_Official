package com.example.fourthofficial.domain.rules

import com.example.fourthofficial.domain.event.Score
import com.example.fourthofficial.domain.event.ScoreType

fun getScoreTypePoints(type: ScoreType): Int = when (type) {
    ScoreType.TRY -> 5
    ScoreType.CONVERSION_MADE -> 2
    ScoreType.PENALTY_MADE -> 3
    ScoreType.PENALTY_TRY -> 5
    ScoreType.DROP_GOAL_MADE -> 3
    else -> 0
}

fun calculateScore(events: List<Score>): Int {
    return events.sumOf { getScoreTypePoints(it.type) }
}