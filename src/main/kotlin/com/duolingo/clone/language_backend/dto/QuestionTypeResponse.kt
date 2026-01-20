package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class QuestionTypeResponse(
    val id: UUID,
    val typeName: String,
    val description: String?
)
