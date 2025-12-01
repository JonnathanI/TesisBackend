package com.duolingo.clone.language_backend.dto

import java.time.Instant
import java.util.UUID

data class LessonProgressDTO(
    // Propiedades de la Lección (LessonEntity)
    val id: UUID,
    val title: String,
    val lessonOrder: Int,
    val requiredXp: Int,

    // Propiedades del Progreso del Usuario (UserLessonProgressEntity)
    val isCompleted: Boolean,
    val masteryLevel: Int,
    val lastPracticed: Instant?
)