package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.EvaluationAnswerEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EvaluationAnswerRepository : JpaRepository<EvaluationAnswerEntity, UUID> {

    fun findByAssignmentId(assignmentId: UUID): List<EvaluationAnswerEntity>
}
