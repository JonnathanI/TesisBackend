package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.UserBadgeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserBadgeRepository : JpaRepository<UserBadgeEntity, UUID> {
    fun existsByUserIdAndBadgeId(userId: UUID, badgeId: UUID): Boolean
    fun findAllByUserId(userId: UUID): List<UserBadgeEntity>
}

