package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class CreateClassroomRequest(
    val name: String,
    val courseId: UUID   // 👈 importante: debe ser UUID
)

