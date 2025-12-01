package com.duolingo.clone.language_backend.dto

// DTO para el inicio de sesión
data class LoginRequest(
    val email: String,
    val password: String
)