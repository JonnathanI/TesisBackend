package com.duolingo.clone.language_backend.dto

data class SingleChallengeDTO(
    val id: Int,
    val type: String,   // "XP", "TIME", "PERFECT", "SQL"
    val title: String,
    val progress: Int,
    val total: Int
)