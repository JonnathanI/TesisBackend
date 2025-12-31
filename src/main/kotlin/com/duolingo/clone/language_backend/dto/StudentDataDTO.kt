package com.duolingo.clone.language_backend.dto

import java.util.UUID

// DTO para enviar solo los datos necesarios al frontend
data class StudentDataDTO(
    val id: UUID,
    val fullName: String,
    val email: String,
    val xpTotal: Long,
    val currentStreak: Int,
    val heartsCount:Int,
    val lingotsCount: Int,
    val isActive: Boolean
)