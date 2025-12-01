package com.duolingo.clone.language_backend.dto

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    // Nuevo campo opcional para el código de administrador
    val adminCode: String? = null
)