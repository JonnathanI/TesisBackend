package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class AssignmentSummaryDTO(
    val id: UUID,
    val title: String,
    val description: String,
    val xpReward: Int,
    val dueDate: String
)
