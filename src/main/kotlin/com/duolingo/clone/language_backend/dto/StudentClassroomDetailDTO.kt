package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class StudentClassroomDetailDTO(
    val id: UUID,
    val name: String,
    val code: String,
    val teacherName: String,
    val students: List<StudentSummaryDTO>,
    val assignments: List<AssignmentSummaryDTO>
)

