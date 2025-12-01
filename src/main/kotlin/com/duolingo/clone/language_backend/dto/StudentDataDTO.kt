package com.duolingo.clone.language_backend.dto

import java.util.UUID

// DTO para enviar solo los datos necesarios al frontend
data class StudentDataDTO(
    val id: UUID,
    val fullName: String,
    val xpTotal: Int,
    val currentStreak: Int
)