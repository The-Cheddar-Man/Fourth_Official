package com.thecheddarman.fourthofficial.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_players",
    foreignKeys = [
        ForeignKey(
            entity = SavedTeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["teamId"])]
)
data class SavedPlayerEntity(
    @PrimaryKey
    val id: String,
    val teamId: String,
    val name: String,
    val defaultShirtNumber: Int,
    val rosterOrder: Int
)