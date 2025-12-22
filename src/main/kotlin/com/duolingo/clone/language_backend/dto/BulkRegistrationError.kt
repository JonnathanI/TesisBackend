package com.duolingo.clone.language_backend.dto

data class BulkRegistrationError(
    val email: String,
    val message: String
)
