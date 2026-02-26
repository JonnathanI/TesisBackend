package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class UnitDTO(
    val id: UUID,
    val courseId: UUID,
    val title: String,
    val unitOrder: Int
)