// src/main/kotlin/com/duolingo/clone/language_backend/dto/UpdateUnitRequest.kt
package com.duolingo.clone.language_backend.dto

data class UpdateUnitRequest(
    val title: String,
    val unitOrder: Int
)