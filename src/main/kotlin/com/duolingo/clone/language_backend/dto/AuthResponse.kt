    package com.duolingo.clone.language_backend.dto

    // DTO de respuesta para el login/registro (contendrá el token JWT)
    data class AuthResponse(
        val token: String,
        val userId: String,
        val role: String,
        val fullName: String
    )
