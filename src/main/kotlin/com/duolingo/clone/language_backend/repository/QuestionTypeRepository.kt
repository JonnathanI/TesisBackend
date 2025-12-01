package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.QuestionTypeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface QuestionTypeRepository : JpaRepository<QuestionTypeEntity, String>