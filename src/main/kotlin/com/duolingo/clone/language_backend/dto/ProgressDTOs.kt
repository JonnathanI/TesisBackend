package com.duolingo.clone.language_backend.dto


import java.time.LocalDateTime
import java.util.UUID

data class DetailedStudentProgressDTO(
    val units: List<UnitProgressDTO>
)

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
