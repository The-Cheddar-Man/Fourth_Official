package com.example.fourthofficial.domain.id

import java.util.UUID

@JvmInline
value class PlayerId(val value: String) {
    companion object {
        fun new() = PlayerId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class TeamId(val value: String) {
    companion object {
        fun new() = TeamId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class MatchId(val value: String) {
    companion object {
        fun new() = MatchId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class EventId(val value: String) {
    companion object {
        fun new() = EventId(UUID.randomUUID().toString())
    }
}