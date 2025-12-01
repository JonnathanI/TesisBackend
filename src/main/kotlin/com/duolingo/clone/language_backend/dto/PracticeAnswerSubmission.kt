package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class PracticeAnswerSubmission(
    val questionId: UUID,
    val userAnswer: String,
    val isCorrect: Boolean // El frontend debe enviar si fue correcta o no
)