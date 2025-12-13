package com.duolingo.clone.language_backend.dto

import java.util.UUID

// DTO para el resumen del estudiante dentro de una clase
data class StudentSummaryDTO(
    val id: UUID,
    val fullName: String,
    val email: String,
    val xpTotal: Long
)