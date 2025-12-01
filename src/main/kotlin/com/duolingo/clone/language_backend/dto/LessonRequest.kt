package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class LessonRequest(
    val unitId: UUID,
    val title: String,
    val lessonOrder: Int,
    val requiredXp: Int = 10
)