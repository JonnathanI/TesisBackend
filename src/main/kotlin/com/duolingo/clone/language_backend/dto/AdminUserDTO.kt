package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class AdminUserDTO(
    val id: UUID,
    val fullName: String?,
    val email: String?,
    val username: String?,
    val cedula: String?,
    val role: String,
    val xpTotal: Int,          // 👈 CAMBIAR A LONG
    val currentStreak: Int,
    val isActive: Boolean
)

