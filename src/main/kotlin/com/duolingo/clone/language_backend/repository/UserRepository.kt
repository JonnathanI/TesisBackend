package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.*
import com.duolingo.clone.language_backend.enums.Role // <-- ¡IMPORTACIÓN CORREGIDA!
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

// Asumo que tu entidad UserEntity ya usa el enum Role
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?

    // Usamos el tipo Role (que acabamos de importar)
    fun findByRole(role: Role): List<UserEntity>

    fun findTop5ByOrderByXpTotalDesc(): List<UserEntity>
}