package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class LeaderboardEntryDTO(
    val userId: UUID,
    val fullName: String,
    val xpTotal: Long,
    val position: Int // 1, 2, 3...
)