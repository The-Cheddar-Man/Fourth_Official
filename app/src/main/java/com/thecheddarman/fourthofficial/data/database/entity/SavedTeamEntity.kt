package com.thecheddarman.fourthofficial.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "saved_teams", indices = [Index(value = ["name"], unique = true)])
data class SavedTeamEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val abbreviation: String?,
    val colourHex: String
)