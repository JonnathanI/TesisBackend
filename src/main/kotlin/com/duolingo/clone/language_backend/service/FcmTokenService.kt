// src/main/kotlin/com/duolingo/clone/language_backend/service/FcmTokenService.kt
package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.entity.FcmTokenEntity
import com.duolingo.clone.language_backend.repository.FcmTokenRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FcmTokenService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val userRepository: UserRepository
) {

    fun registerToken(userId: UUID, token: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        // Si ya existe ese token, lo reactivamos
        val existing = fcmTokenRepository.findByUserAndToken(user, token)
        if (existing != null) {
            existing.active = true
            fcmTokenRepository.save(existing)
            return
        }

        // Si no existe, lo creamos
        val entity = FcmTokenEntity(
            user = user,
            token = token,
            active = true
        )
        fcmTokenRepository.save(entity)
    }
}