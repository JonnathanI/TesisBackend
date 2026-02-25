package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.entity.FcmTokenEntity
import com.duolingo.clone.language_backend.repository.FcmTokenRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FcmTokenService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun registerToken(userId: UUID, token: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        // 1️⃣ Primero buscamos por TOKEN (porque en BD es único)
        val existingByToken = fcmTokenRepository.findByToken(token)

        if (existingByToken != null) {
            // Si ya existe ese token, lo "reutilizamos"
            // y lo asociamos al usuario actual (por ejemplo, si cambió de cuenta en el mismo dispositivo)
            existingByToken.user = user        // 🔴 IMPORTANTE: user debe ser var en FcmTokenEntity
            existingByToken.active = true      // se asegura de que esté activo

            fcmTokenRepository.save(existingByToken)

            println("ℹ️ Token FCM ya existía, se reasigna a ${user.email}: $token")
            return
        }

        // 2️⃣ Si no existe el token, recién ahí creamos un registro nuevo
        val entity = FcmTokenEntity(
            user = user,
            token = token,
            active = true
        )

        fcmTokenRepository.save(entity)
        println("✅ Nuevo token FCM registrado para ${user.email}: $token")
    }
}