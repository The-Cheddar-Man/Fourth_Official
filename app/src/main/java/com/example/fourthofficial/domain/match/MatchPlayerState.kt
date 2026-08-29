package com.example.fourthofficial.domain.match

import com.example.fourthofficial.domain.id.PlayerId

data class MatchPlayerState (
    val playerId: PlayerId,
    val isOnField: Boolean = false,
    val fieldPos: Int? = null,
    val yellowUntilPlayingMs: Long? = null,
    val isRedCarded: Boolean = false
)