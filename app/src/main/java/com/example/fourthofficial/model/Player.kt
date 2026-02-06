package com.example.fourthofficial.model

data class Player(
    val name: String,
    val number: Int,
    val isOnField: Boolean = false,
    val fieldPos: Int? = null,
    val yellowUntilHalfMs: Long? = null,
    val isRedCarded: Boolean = false
)