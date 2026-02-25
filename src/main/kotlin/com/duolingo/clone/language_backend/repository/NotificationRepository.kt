package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.NotificationEntity
import com.duolingo.clone.language_backend.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationRepository : JpaRepository<NotificationEntity, UUID> {
    fun findByUserOrderByCreatedAtDesc(user: UserEntity): List<NotificationEntity>
    fun countByUserAndReadIsFalse(user: UserEntity): Long
}