package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class UnitStatusDTO(
    val id: UUID,
    val title: String,
    val unitOrder: Int,
    val isLocked: Boolean,
    val isCompleted: Boolean,
    val lessons: List<LessonProgressDTO>
)