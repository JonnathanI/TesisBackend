package com.duolingo.clone.language_backend.dto

data class UpdateUserRequest(
    val fullName: String,
    val email: String,
    val cedula: String,
)

