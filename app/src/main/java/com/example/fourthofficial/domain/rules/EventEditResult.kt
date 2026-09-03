package com.example.fourthofficial.domain.rules

sealed interface EventEditResult {
    data object Success : EventEditResult
    data class Failure(val message: String) : EventEditResult
}