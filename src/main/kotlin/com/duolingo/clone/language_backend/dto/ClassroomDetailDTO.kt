package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class ClassroomDetailDTO(
    val id: UUID,
    val name: String,
    val code: String,
    val students: List<StudentSummaryDTO>
)