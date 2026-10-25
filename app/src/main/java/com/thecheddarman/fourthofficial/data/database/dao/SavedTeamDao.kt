package com.thecheddarman.fourthofficial.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.thecheddarman.fourthofficial.data.database.entity.SavedPlayerEntity
import com.thecheddarman.fourthofficial.data.database.entity.SavedTeamEntity

@Dao
interface SavedTeamDao {

    @Insert
    suspend fun insertTeam(team: SavedTeamEntity)

    @Insert
    suspend fun insertPlayers(players: List<SavedPlayerEntity>)

    @Transaction
    suspend fun insertTeamWithPlayers(team: SavedTeamEntity, players: List<SavedPlayerEntity>)
    {
        insertTeam(team)
        insertPlayers(players)
    }

    @Update
    suspend fun updateTeam(team: SavedTeamEntity)

    @Update
    suspend fun updatePlayers(players: List<SavedPlayerEntity>)

    @Query("DELETE FROM saved_players WHERE teamId = :teamId")
    suspend fun deletePlayersForTeam(teamId: String)

    @Transaction
    suspend fun replacePlayersForTeam(teamId: String, players: List<SavedPlayerEntity>)
    {
        deletePlayersForTeam(teamId)
        insertPlayers(players)
    }

    @Query("DELETE FROM saved_teams WHERE id = :teamId")
    suspend fun deleteTeamById(teamId: String): Int

    @Query("SELECT * FROM saved_teams ORDER BY name")
    suspend fun getAllTeams(): List<SavedTeamEntity>

    @Query("SELECT * FROM saved_teams WHERE id = :teamId")
    suspend fun getTeamById(teamId: String): SavedTeamEntity?

    @Query("SELECT * FROM saved_players WHERE teamId = :teamId ORDER BY rosterOrder")
    suspend fun getPlayersForTeam(teamId: String): List<SavedPlayerEntity>
}