package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.FcmTokenEntity
import com.duolingo.clone.language_backend.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FcmTokenRepository : JpaRepository<FcmTokenEntity, UUID> {

    // 🔹 Lo puedes seguir usando si lo necesitas
    fun findByUserAndToken(user: UserEntity, token: String): FcmTokenEntity?

    // 🔹 NUEVO: para respetar el UNIQUE(token)
    fun findByToken(token: String): FcmTokenEntity?

    fun findAllByUserAndActiveIsTrue(user: UserEntity): List<FcmTokenEntity>
}