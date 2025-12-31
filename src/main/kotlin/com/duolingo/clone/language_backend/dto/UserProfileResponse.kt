package com.duolingo.clone.language_backend.dto

import java.time.Instant
import java.time.LocalDateTime

data class UserProfileResponse(
    val fullName: String,
    val username: String,
    val joinedAt: Instant?,
    val totalXp: Long,
    val currentStreak: Int,
    val lingots: Int,
    val heartsCount: Int,
    val league: String,
    val avatarData: String?
)