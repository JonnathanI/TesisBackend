package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class EvaluationRequest(
    val title: String,
    val description: String?,
    val classroomId: UUID?,   // 🔥 aula donde se publicará la evaluación
    val questions: List<EvaluationQuestionRequest> = emptyList()
)