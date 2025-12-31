package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.RegistrationCodeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID
import java.util.Optional

interface RegistrationCodeRepository : JpaRepository<RegistrationCodeEntity, UUID> {
    fun findByCode(code: String): Optional<RegistrationCodeEntity>
}