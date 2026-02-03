// dto/CompleteEvaluationRequest.kt
package com.duolingo.clone.language_backend.dto

import java.math.BigDecimal
import java.util.UUID

data class AnswerSubmitDTO(
    val questionId: UUID,
    val answerText: String // lo que el alumno respondió
)

data class CompleteEvaluationRequest(
    val score: BigDecimal?,
    val status: String,
    val answers: List<AnswerSubmitDTO>
)
