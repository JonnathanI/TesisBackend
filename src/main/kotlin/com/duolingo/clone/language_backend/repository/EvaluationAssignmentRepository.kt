package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.dto.PendingEvaluationDTO
import com.duolingo.clone.language_backend.entity.EvaluationAssignmentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface EvaluationAssignmentRepository : JpaRepository<EvaluationAssignmentEntity, UUID> {

    fun findByStudentIdAndCompletedFalse(studentId: UUID): List<EvaluationAssignmentEntity>

    // ✅ NUEVO MÉTODO (el que SÍ debe usar el frontend)
    @Query("""
        SELECT new com.duolingo.clone.language_backend.dto.PendingEvaluationDTO(
            ea.id,
            e.id,
            e.title,
            e.description,
            ea.dueDate,
            ea.completed,
            ea.score
        )
        FROM EvaluationAssignmentEntity ea
        JOIN ea.evaluation e
        WHERE ea.student.id = :studentId
        AND ea.completed = false
        ORDER BY ea.dueDate
    """)
    fun findPendingEvaluationsForStudent(
        @Param("studentId") studentId: UUID
    ): List<PendingEvaluationDTO>

    // 🔹 Nueva: TODAS las evaluaciones del alumno
    @Query("""
        SELECT NEW com.duolingo.clone.language_backend.dto.PendingEvaluationDTO(
            ea.id,
            e.id,
            e.title,
            e.description,
            ea.dueDate,
            ea.completed,
            ea.score
        )
        FROM EvaluationAssignmentEntity ea
        JOIN ea.evaluation e
        WHERE ea.student.id = :studentId
        ORDER BY ea.assignedAt DESC
    """)
    fun findAllEvaluationsForStudent(@Param("studentId") studentId: UUID): List<PendingEvaluationDTO>
}
