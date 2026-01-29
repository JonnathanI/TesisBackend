package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.EvaluationAssignmentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EvaluationAssignmentRepository : JpaRepository<EvaluationAssignmentEntity, UUID> {
    // Para que el alumno vea sus exámenes pendientes
    fun findByStudentIdAndCompletedFalse(studentId: UUID): List<EvaluationAssignmentEntity>
}