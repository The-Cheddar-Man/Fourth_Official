package com.thecheddarman.fourthofficial.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "matches",
    indices = [Index(value = ["status"])]
)
data class MatchEntity(
    @PrimaryKey
    val id: String,
    val status: String,
    val phase: String,
    val firstHalfElapsedMs: Long,
    val secondHalfElapsedMs: Long,
    val createdAtEpochMs: Long,
    val completedAtEpochMs: Long?
)