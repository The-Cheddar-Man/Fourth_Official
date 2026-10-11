package com.thecheddarman.fourthofficial.dev

import com.thecheddarman.fourthofficial.domain.event.Discipline
import com.thecheddarman.fourthofficial.domain.event.DisciplineReasonRed
import com.thecheddarman.fourthofficial.domain.event.DisciplineReasonYellow
import com.thecheddarman.fourthofficial.domain.event.DisciplineType
import com.thecheddarman.fourthofficial.domain.event.MatchEvent
import com.thecheddarman.fourthofficial.domain.event.Score
import com.thecheddarman.fourthofficial.domain.event.ScoreType
import com.thecheddarman.fourthofficial.domain.event.Substitution
import com.thecheddarman.fourthofficial.domain.event.SubstitutionType
import com.thecheddarman.fourthofficial.domain.id.PlayerId
import com.thecheddarman.fourthofficial.domain.team.Player
import com.thecheddarman.fourthofficial.domain.team.Team

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
                Player(number = 1, name = "Adam Mercer"),
                Player(number = 2, name = "Ben Holloway"),
                Player(number = 3, name = "Callum Price"),
                Player(number = 4, name = "Daniel Reeves"),
                Player(number = 5, name = "Elliot Fraser"),
                Player(number = 6, name = "Finley Hart"),
                Player(number = 7, name = "George Nolan"),
                Player(number = 8, name = "Harry Sutton"),
                Player(number = 9, name = "Isaac Moore"),
                Player(number = 10, name = "Jack Turner"),
                Player(number = 11, name = "Kai Bennett"),
                Player(number = 12, name = "Lewis Grant"),
                Player(number = 13, name = "Matthew Ellis"),
                Player(number = 14, name = "Nathan Cole"),
                Player(number = 15, name = "Oliver Dean"),
                Player(number = 16, name = "Peter Walsh"),
                Player(number = 17, name = "Ryan Foster"),
                Player(number = 18, name = "Samuel Boyd"),
                Player(number = 19, name = "Thomas Kerr"),
                Player(number = 20, name = "William Hayes"),
                Player(number = 21, name = "Zachary Mills"),
                Player(number = 22, name = "Connor Blake"),
                Player(number = 23, name = "Jamie Rhodes")
            )
        )

        val billingham = Team(
            name = "Billingham",
            index = 2,
            players = listOf(
                Player(number = 1, name = "Aaron Mitchell"),
                Player(number = 2, name = "Bradley Shaw"),
                Player(number = 3, name = "Cameron Lewis"),
                Player(number = 4, name = "Dylan Parker"),
                Player(number = 5, name = "Ethan Brooks"),
                Player(number = 6, name = "Freddie Cooper"),
                Player(number = 7, name = "Gareth Wilson"),
                Player(number = 8, name = "Henry Morris"),
                Player(number = 9, name = "Jacob Palmer"),
                Player(number = 10, name = "Kieran Scott"),
                Player(number = 11, name = "Liam Walker"),
                Player(number = 12, name = "Max Carter"),
                Player(number = 13, name = "Noah Evans"),
                Player(number = 14, name = "Owen Clarke"),
                Player(number = 15, name = "Patrick Hughes"),
                Player(number = 16, name = "Reece Morgan"),
                Player(number = 17, name = "Scott Bailey"),
                Player(number = 18, name = "Theo Russell"),
                Player(number = 19, name = "Victor James"),
                Player(number = 20, name = "Will Dawson"),
                Player(number = 21, name = "Alex Chapman"),
                Player(number = 22, name = "Charlie Mason"),
                Player(number = 23, name = "Josh Harper")
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