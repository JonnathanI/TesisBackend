package com.duolingo.clone.language_backend.dto

data class BulkUserItem(
    val email: String,
    val fullName: String,
    val password: String,
    val cedula: String,
)