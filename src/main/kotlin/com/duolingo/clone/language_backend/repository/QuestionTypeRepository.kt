package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.QuestionTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface QuestionTypeRepository : JpaRepository<QuestionTypeEntity, UUID> {

    fun findByTypeName(typeName: String): QuestionTypeEntity?
}
