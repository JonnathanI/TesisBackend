package com.duolingo.clone.language_backend.dto

import java.util.*

data class EvaluationQuestionDTO(
    val id: UUID,
    val textSource: String,
    val textTarget: String?,
    val options: List<String>,
    val questionType: String
)
