package com.duolingo.clone.language_backend.dto

data class PasswordResetConfirmDTO(
    val token: String,
    val newPassword: String
)
