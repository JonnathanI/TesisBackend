// src/main/kotlin/com/duolingo/clone/language_backend/dto/CreateCourseDTO.kt
package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class CreateCourseDTO(
    val title: String,
    val targetLanguage: String,
    val baseLanguage: String,

    val teachers: List<UUID> = emptyList(),
    val students: List<UUID> = emptyList()
)

