package com.thecheddarman.fourthofficial.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "match_teams",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["matchId"]),
        Index(value = ["matchId", "teamIndex"], unique = true)
    ]
)
data class MatchTeamEntity(
    @PrimaryKey
    val id: String,
    val matchId: String,
    val name: String,
    val abbreviation: String?,
    val colourHex: String,
    val teamIndex: Int
)