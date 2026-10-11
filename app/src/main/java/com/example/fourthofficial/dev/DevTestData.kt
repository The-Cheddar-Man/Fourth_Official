package com.example.fourthofficial.dev

import com.example.fourthofficial.domain.event.Discipline
import com.example.fourthofficial.domain.event.DisciplineReasonRed
import com.example.fourthofficial.domain.event.DisciplineReasonYellow
import com.example.fourthofficial.domain.event.DisciplineType
import com.example.fourthofficial.domain.event.MatchEvent
import com.example.fourthofficial.domain.event.Score
import com.example.fourthofficial.domain.event.ScoreType
import com.example.fourthofficial.domain.event.Substitution
import com.example.fourthofficial.domain.event.SubstitutionType
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.team.Player
import com.example.fourthofficial.domain.team.Team

data class DevTestTeams(
    val team1: Team,
    val team2: Team
)

object DevTestData {
    fun createTeams(): DevTestTeams {
        val mowdenPark = Team(
            name = "Mowden Park",
            index = 1,
            players = listOf(
                Player(number = 1, name = "Alfie Barron"),
                Player(number = 2, name = "Bruno Brown"),
                Player(number = 3, name = "Sam Clark"),
                Player(number = 4, name = "Joe Craggs"),
                Player(number = 5, name = "Luke Coulston"),
                Player(number = 6, name = "Jacob Butterill"),
                Player(number = 7, name = "Drew Davison"),
                Player(number = 8, name = "Alex Fusco"),
                Player(number = 9, name = "Ben Douglas"),
                Player(number = 10, name = "Junior Newton"),
                Player(number = 11, name = "Harvey Johnston"),
                Player(number = 12, name = "Joeli Varo"),
                Player(number = 13, name = "Liam Checksfield"),
                Player(number = 14, name = "Shaun McCartney"),
                Player(number = 15, name = "Tyler Armstrong"),
                Player(number = 16, name = "Jacob Oliver"),
                Player(number = 17, name = "Henry Turnbull"),
                Player(number = 18, name = "Harrison Wood"),
                Player(number = 19, name = "Dylan Thompson"),
                Player(number = 20, name = "Kieran Forbes"),
                Player(number = 21, name = "Harrison Mason"),
                Player(number = 22, name = "Callum Mackenzie"),
                Player(number = 23, name = "Joshua Bailey")
            )
        )

        val billingham = Team(
            name = "Billingham",
            index = 2,
            players = listOf(
                Player(number = 1, name = "James Boyd-Bell"),
                Player(number = 2, name = "Tom Fawcett"),
                Player(number = 3, name = "Kieran Clark"),
                Player(number = 4, name = "George Bolam"),
                Player(number = 5, name = "Ryan Kirkbride"),
                Player(number = 6, name = "Liam Armstrong"),
                Player(number = 7, name = "Nathan Myers"),
                Player(number = 8, name = "Anthony Jackson"),
                Player(number = 9, name = "Jamie Lane"),
                Player(number = 10, name = "Peter Evans"),
                Player(number = 11, name = "Martin Hopley"),
                Player(number = 12, name = "Luke Wilson"),
                Player(number = 13, name = "Harry Mayes"),
                Player(number = 14, name = "Joseph Scarborough"),
                Player(number = 15, name = "Elliott Husband"),
                Player(number = 16, name = "Ben Hyndman"),
                Player(number = 17, name = "Adam Sunley"),
                Player(number = 18, name = "Kyle Scott"),
                Player(number = 19, name = "Lauchlan Hill"),
                Player(number = 20, name = "Joshua Wilson"),
                Player(number = 21, name = "Sam Wilson"),
                Player(number = 22, name = "Louie Plews"),
                Player(number = 23, name = "Harry Rickard")
            )
        )

        return DevTestTeams(
            team1 = mowdenPark,
            team2 = billingham
        )
    }

    fun createSummaryEvents(team1: Team, team2: Team, halfDurationMs: Long): List<MatchEvent> {
        val team1Id = team1.id
        val team2Id = team2.id

        return listOf(
            // FIRST HALF — MOWDEN PARK
            Score(
                timeMs = time(5, 20),
                teamId = team1Id,
                halfIndex = 1,
                playerId = team1.playerId(10),
                type = ScoreType.PENALTY_MADE
            ),

            Score(
                timeMs = time(12, 40),
                teamId = team1Id,
                halfIndex = 1,
                playerId = team1.playerId(11),
                type = ScoreType.TRY
            ),

            Score(
                timeMs = time(13, 55),
                teamId = team1Id,
                halfIndex = 1,
                playerId = team1.playerId(10),
                type = ScoreType.CONVERSION_MADE
            ),

            Score(
                timeMs = time(21, 10),
                teamId = team1Id,
                halfIndex = 1,
                playerId = team1.playerId(10),
                type = ScoreType.PENALTY_MISSED
            ),

            Discipline(
                timeMs = time(29, 30),
                teamId = team1Id,
                halfIndex = 1,
                playerId = team1.playerId(6),
                type = DisciplineType.YELLOW,
                reason = DisciplineReasonYellow.TECHNICAL
            ),

            Score(
                timeMs = time(35, 45),
                teamId = team1Id,
                halfIndex = 1,
                playerId = team1.playerId(8),
                type = ScoreType.TRY
            ),

            Score(
                timeMs = time(36, 55),
                teamId = team1Id,
                halfIndex = 1,
                playerId = team1.playerId(10),
                type = ScoreType.CONVERSION_MISSED
            ),

            Substitution(
                timeMs = time(38, 10),
                teamId = team1Id,
                halfIndex = 1,
                playerOffId = team1.playerId(11),
                playerOnId = team1.playerId(22),
                type = SubstitutionType.TACTICAL
            ),

            // FIRST HALF — BILLINGHAM
            Score(
                timeMs = time(8, 5),
                teamId = team2Id,
                halfIndex = 1,
                playerId = team2.playerId(10),
                type = ScoreType.PENALTY_MADE
            ),

            Score(
                timeMs = time(16, 50),
                teamId = team2Id,
                halfIndex = 1,
                playerId = team2.playerId(8),
                type = ScoreType.TRY
            ),

            Score(
                timeMs = time(18, 0),
                teamId = team2Id,
                halfIndex = 1,
                playerId = team2.playerId(10),
                type = ScoreType.CONVERSION_MADE
            ),

            Discipline(
                timeMs = time(25, 25),
                teamId = team2Id,
                halfIndex = 1,
                playerId = team2.playerId(7),
                type = DisciplineType.YELLOW,
                reason = DisciplineReasonYellow.FOUL_PLAY
            ),

            Score(
                timeMs = time(34, 10),
                teamId = team2Id,
                halfIndex = 1,
                playerId = team2.playerId(10),
                type = ScoreType.PENALTY_MISSED
            ),

            Substitution(
                timeMs = time(36, 20),
                teamId = team2Id,
                halfIndex = 1,
                playerOffId = team2.playerId(14),
                playerOnId = team2.playerId(23),
                type = SubstitutionType.TACTICAL
            ),

            Score(
                timeMs = time(39, 5),
                teamId = team2Id,
                halfIndex = 1,
                playerId = team2.playerId(12),
                type = ScoreType.TRY
            ),

            Score(
                timeMs = time(39, 55),
                teamId = team2Id,
                halfIndex = 1,
                playerId = team2.playerId(10),
                type = ScoreType.CONVERSION_MISSED
            ),

            // HALF TIME
            Substitution(
                timeMs = halfDurationMs,
                teamId = team1Id,
                halfIndex = 2,
                playerOffId = team1.playerId(2),
                playerOnId = team1.playerId(16),
                type = SubstitutionType.TACTICAL,
                occurredAtHalfTime = true
            ),

            Substitution(
                timeMs = halfDurationMs,
                teamId = team2Id,
                halfIndex = 2,
                playerOffId = team2.playerId(5),
                playerOnId = team2.playerId(19),
                type = SubstitutionType.TACTICAL,
                occurredAtHalfTime = true
            ),

            // SECOND HALF — MOWDEN PARK
            Score(
                timeMs = halfDurationMs + time(4, 20),
                teamId = team1Id,
                halfIndex = 2,
                playerId = team1.playerId(22),
                type = ScoreType.TRY
            ),

            Score(
                timeMs = halfDurationMs + time(5, 30),
                teamId = team1Id,
                halfIndex = 2,
                playerId = team1.playerId(10),
                type = ScoreType.CONVERSION_MADE
            ),

            Substitution(
                timeMs = halfDurationMs + time(12, 10),
                teamId = team1Id,
                halfIndex = 2,
                playerOffId = team1.playerId(3),
                playerOnId = team1.playerId(17),
                type = SubstitutionType.INJURY
            ),

            Score(
                timeMs = halfDurationMs + time(17, 40),
                teamId = team1Id,
                halfIndex = 2,
                playerId = team1.playerId(10),
                type = ScoreType.PENALTY_MADE
            ),

            Score(
                timeMs = halfDurationMs + time(18, 20),
                teamId = team1Id,
                halfIndex = 2,
                playerId = null,
                type = ScoreType.PENALTY_TRY
            ),

            Substitution(
                timeMs = halfDurationMs + time(21, 5),
                teamId = team1Id,
                halfIndex = 2,
                playerOffId = team1.playerId(12),
                playerOnId = team1.playerId(23),
                type = SubstitutionType.HIA
            ),

            Discipline(
                timeMs = halfDurationMs + time(24, 30),
                teamId = team1Id,
                halfIndex = 2,
                playerId = team1.playerId(7),
                type = DisciplineType.RED,
                reason = DisciplineReasonRed.DANGEROUS_PLAY
            ),

            Discipline(
                timeMs = halfDurationMs + time(25, 10),
                teamId = team1Id,
                halfIndex = 2,
                playerId = team1.playerId(5),
                type = DisciplineType.YELLOW,
                reason = DisciplineReasonYellow.FOUL_PLAY
            ),

            Score(
                timeMs = halfDurationMs + time(26, 15),
                teamId = team1Id,
                halfIndex = 2,
                playerId = team1.playerId(10),
                type = ScoreType.DROP_GOAL_MISSED
            ),

            // SECOND HALF — BILLINGHAM
            Score(
                timeMs = halfDurationMs + time(7, 10),
                teamId = team2Id,
                halfIndex = 2,
                playerId = team2.playerId(9),
                type = ScoreType.TRY
            ),

            Score(
                timeMs = halfDurationMs + time(8, 20),
                teamId = team2Id,
                halfIndex = 2,
                playerId = team2.playerId(10),
                type = ScoreType.CONVERSION_MISSED
            ),

            Discipline(
                timeMs = halfDurationMs + time(10, 0),
                teamId = team2Id,
                halfIndex = 2,
                playerId = team2.playerId(4),
                type = DisciplineType.YELLOW,
                reason = DisciplineReasonYellow.TECHNICAL
            ),

            Substitution(
                timeMs = halfDurationMs + time(15, 30),
                teamId = team2Id,
                halfIndex = 2,
                playerOffId = team2.playerId(1),
                playerOnId = team2.playerId(17),
                type = SubstitutionType.INJURY
            ),

            Discipline(
                timeMs = halfDurationMs + time(20, 30),
                teamId = team2Id,
                halfIndex = 2,
                playerId = team2.playerId(4),
                type = DisciplineType.YELLOW,
                reason = DisciplineReasonYellow.FOUL_PLAY
            ),

            Score(
                timeMs = halfDurationMs + time(22, 45),
                teamId = team2Id,
                halfIndex = 2,
                playerId = team2.playerId(10),
                type = ScoreType.PENALTY_MADE
            ),

            Substitution(
                timeMs = halfDurationMs + time(25, 20),
                teamId = team2Id,
                halfIndex = 2,
                playerOffId = team2.playerId(9),
                playerOnId = team2.playerId(21),
                type = SubstitutionType.TACTICAL
            )
        )
    }

    private fun Team.playerId(number: Int): PlayerId {
        return players.first { player -> player.number == number }.id
    }

    private fun time(minutes: Int, seconds: Int = 0): Long {
        return (minutes * 60L + seconds) * 1000L
    }
}