package com.example.fourthofficial.domain.match

import com.example.fourthofficial.domain.id.MatchId
import com.example.fourthofficial.model.Discipline
import com.example.fourthofficial.model.Score
import com.example.fourthofficial.model.Substitution

data class MatchState(
    val id: MatchId = MatchId.new(),

    val phase: MatchPhase = MatchPhase.NOT_STARTED,
    val clock: MatchClock = MatchClock(),

    val team1: MatchTeamState,
    val team2: MatchTeamState,

    val scoreEvents: List<Score> = emptyList(),
    val subEvents: List<Substitution> = emptyList(),
    val discEvents: List<Discipline> = emptyList()
)