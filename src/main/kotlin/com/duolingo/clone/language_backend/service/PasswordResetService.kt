package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.entity.PasswordResetTokenEntity
import com.duolingo.clone.language_backend.repository.PasswordResetTokenRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val tokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${application.frontend-url}")
    private val frontendUrl: String
) {

    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email.lowercase().trim())
            ?: return

        val token = UUID.randomUUID().toString()
        val resetToken = PasswordResetTokenEntity(
            token = token,
            user = user,
            expiresAt = LocalDateTime.now().plusMinutes(5)
        )

        tokenRepository.save(resetToken)

        // ✅ Ahora usamos la variable dinámica
        val resetLink = "$frontendUrl/reset-password?token=$token"

        emailService.sendEmail(
            user.email,
            "Recuperación de contraseña",
            """
            Hola ${user.fullName},

            Para restablecer tu contraseña, haz clic en el enlace:

            $resetLink

            Este enlace expira en 5 minutos.
            """.trimIndent()
        )
    }

    fun resetPassword(token: String, newPassword: String) {

        val resetToken = tokenRepository.findByToken(token)
            ?: throw RuntimeException("Token inválido")

        if (resetToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw RuntimeException("El token ha expirado")
        }

        val user = resetToken.user
        user.passwordHash = passwordEncoder.encode(newPassword)

        userRepository.save(user)
        tokenRepository.delete(resetToken)
    }
}
