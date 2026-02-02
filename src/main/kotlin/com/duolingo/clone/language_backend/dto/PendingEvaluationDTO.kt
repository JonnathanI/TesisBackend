package com.duolingo.clone.language_backend.dto

import java.time.LocalDateTime
import java.util.UUID
import java.math.BigDecimal

data class PendingEvaluationDTO(
    val assignmentId: UUID,
    val evaluationId: UUID,
    val title: String,
    val description: String?,
    val dueDate: LocalDateTime,
    val completed: Boolean,
    val score: BigDecimal?   // ✅ CAMBIO CLAVE
)
