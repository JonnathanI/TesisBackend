package com.duolingo.clone.language_backend.dto


import java.time.LocalDateTime
import java.util.UUID

data class UnitProgressDTO(
    val id: UUID,
    val title: String,
    val lessons: List<LessonProgressDetailDTO>
)

data class LessonProgressDetailDTO(
    val id: UUID,
    val title: String,
    val isCompleted: Boolean,
    val mistakesCount: Int,
    val correctAnswers: Int,
    val lastPracticed: LocalDateTime?,
    val xpEarned: Int
)

// En el backend (Kotlin)
data class DetailedStudentProgressDTO(
    val fullName: String,
    val username: String,
    val avatarData: String?,
    val totalXp: Int,
    val currentStreak: Int,
    val units: List<UnitProgressDTO> // Tu lista actual de unidades
)
