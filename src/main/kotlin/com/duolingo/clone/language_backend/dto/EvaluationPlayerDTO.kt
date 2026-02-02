package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class EvaluationPlayerDTO(
    val id: UUID,
    val title: String,
    val description: String?,
    val questions: List<EvaluationQuestionDTO>
)
