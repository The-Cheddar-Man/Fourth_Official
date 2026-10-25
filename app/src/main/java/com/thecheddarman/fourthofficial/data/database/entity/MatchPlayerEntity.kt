package com.thecheddarman.fourthofficial.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "match_players",
    foreignKeys = [
        ForeignKey(
            entity = MatchTeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchTeamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["matchTeamId"]),
        Index(value = ["matchTeamId", "rosterOrder"], unique = true)
    ]
)
data class MatchPlayerEntity(
    @PrimaryKey
    val id: String,
    val matchTeamId: String,
    val name: String,
    val shirtNumber: Int,
    val rosterOrder: Int,
    val startingFieldPos: Int?
)