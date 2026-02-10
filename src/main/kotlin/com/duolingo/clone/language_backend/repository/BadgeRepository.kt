package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.BadgeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BadgeRepository : JpaRepository<BadgeEntity, UUID> {
    fun findByCode(code: String): BadgeEntity?
}

