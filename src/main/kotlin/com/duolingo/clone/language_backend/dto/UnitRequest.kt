package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class UnitRequest(
    val courseId: UUID,
    val title: String,
    val unitOrder: Int
)