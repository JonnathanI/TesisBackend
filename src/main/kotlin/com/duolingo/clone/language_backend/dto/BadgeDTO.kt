package com.duolingo.clone.language_backend.dto

data class BadgeDTO(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val earnedAt: Long? // null si NO está desbloqueada
)

