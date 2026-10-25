package com.thecheddarman.fourthofficial.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.thecheddarman.fourthofficial.data.database.entity.MatchEntity
import com.thecheddarman.fourthofficial.data.database.entity.MatchPlayerEntity
import com.thecheddarman.fourthofficial.data.database.entity.MatchTeamEntity

@Dao
interface MatchDao {

    @Insert
    suspend fun insertMatch(match: MatchEntity)

    @Insert
    suspend fun insertTeams(teams: List<MatchTeamEntity>)

    @Insert
    suspend fun insertPlayers(players: List<MatchPlayerEntity>)

    @Transaction
    suspend fun insertMatchStructure(
        match: MatchEntity,
        teams: List<MatchTeamEntity>,
        players: List<MatchPlayerEntity>
    ) {
        insertMatch(match)
        insertTeams(teams)
        insertPlayers(players)
    }

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Update
    suspend fun updateTeam(team: MatchTeamEntity)

    @Update
    suspend fun updatePlayers(players: List<MatchPlayerEntity>)

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: String): MatchEntity?

    @Query("SELECT * FROM match_teams WHERE matchId = :matchId ORDER BY teamIndex")
    suspend fun getTeamsForMatch(matchId: String): List<MatchTeamEntity>

    @Query("SELECT * FROM match_players WHERE matchTeamId = :matchTeamId ORDER BY rosterOrder")
    suspend fun getPlayersForTeam(matchTeamId: String): List<MatchPlayerEntity>

    @Query("SELECT * FROM matches WHERE status = 'ACTIVE'")
    suspend fun getActiveMatches(): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE status = 'COMPLETED' ORDER BY completedAtEpochMs DESC")
    suspend fun getCompletedMatches(): List<MatchEntity>

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatchById(matchId: String): Int
}