package com.duolingo.clone.language_backend.dto

import java.math.BigDecimal
import java.util.UUID

data class QuestionRequest(
    val lessonId: UUID,

    // ✅ ID real de la tabla question_types
    val questionTypeId: UUID,

    val textSource: String,
    val textTarget: String? = null,

    val options: List<String> = emptyList(),

    val audioUrl: String? = null,
    val hintJson: String? = null,
    val difficultyScore: BigDecimal = BigDecimal("0.5"),
    val active: Boolean = true,
)
