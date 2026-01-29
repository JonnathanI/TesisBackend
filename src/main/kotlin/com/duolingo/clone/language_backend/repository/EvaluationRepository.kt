package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.EvaluationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.*
import java.util.UUID

@Repository
interface EvaluationRepository : JpaRepository<EvaluationEntity, UUID>