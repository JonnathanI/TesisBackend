package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class AnswerSubmission(
    val questionId: UUID,
    val userAnswer: String, // La respuesta ingresada por el estudiante
    val isCorrect: Boolean? = null // El servidor lo establece
)