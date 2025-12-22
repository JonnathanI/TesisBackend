package com.duolingo.clone.language_backend.dto

// DTO para la solicitud masiva
data class BulkRegisterRequest(
    val students: List<BulkUserItem>
)
