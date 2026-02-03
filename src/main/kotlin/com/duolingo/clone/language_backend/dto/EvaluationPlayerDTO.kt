package com.duolingo.clone.language_backend.dto

import java.math.BigDecimal
import java.util.UUID

data class EvaluationPlayerDTO(
    val id: UUID,
    val title: String,
    val description: String?,
    val questions: List<EvaluationQuestionDTO>,
    val completed: Boolean = false,            // 👈 nuevo
    val score: BigDecimal? = null
)
