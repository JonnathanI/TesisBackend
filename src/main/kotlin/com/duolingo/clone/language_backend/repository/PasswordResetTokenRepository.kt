package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.PasswordResetTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PasswordResetTokenRepository :
    JpaRepository<PasswordResetTokenEntity, UUID> {

    fun findByToken(token: String): PasswordResetTokenEntity?
}
