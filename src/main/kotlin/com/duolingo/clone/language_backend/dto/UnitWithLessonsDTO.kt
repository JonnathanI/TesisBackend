// UnitWithLessonsDTO.kt
package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class LessonSummaryDTO(
    val id: UUID,
    val title: String,
    val lessonOrder: Int,
    val isCompleted: Boolean
)

data class UnitWithLessonsDTO(
    val id: UUID,
    val title: String,
    val unitOrder: Int,
    val lessons: List<LessonSummaryDTO>
)
