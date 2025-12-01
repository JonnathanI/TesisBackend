package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.UserQuestionDataEntity
import com.duolingo.clone.language_backend.entity.UserQuestionDataId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID
import java.time.LocalDateTime

interface UserQuestionDataRepository : JpaRepository<UserQuestionDataEntity, UserQuestionDataId> {

    // 1. Encontrar preguntas cuya fecha de repaso ya pasó (prioridad máxima)
    fun findTop50ByUserIdAndNextDueDateBeforeOrderByNextDueDateAsc(
        userId: UUID,
        currentDateTime: LocalDateTime
    ): List<UserQuestionDataEntity>

    // 2. Encontrar preguntas del usuario por ID de pregunta
    fun findByUserIdAndQuestionId(userId: UUID, questionId: UUID): UserQuestionDataEntity?

}