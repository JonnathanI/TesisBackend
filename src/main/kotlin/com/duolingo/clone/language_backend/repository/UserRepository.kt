package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun findByRole(role: UserRole): List<UserEntity>
}