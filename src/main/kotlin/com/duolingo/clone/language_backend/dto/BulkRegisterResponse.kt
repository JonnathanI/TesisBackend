package com.duolingo.clone.language_backend.dto

data class BulkRegisterResponse(
    val totalProcessed: Int,
    val successCount: Int,
    val failureCount: Int,
    val errors: List<BulkRegistrationError>
)
