package com.duolingo.clone.language_backend.dto

import java.time.LocalDateTime

data class UserProfileResponse(
    val fullName: String,
    val username: String,
    val joinedAt: LocalDateTime,
    val totalXp: Int,
    val currentStreak: Int,
    val lingots: Int,
    val league: String,
    val avatarData: String?
)