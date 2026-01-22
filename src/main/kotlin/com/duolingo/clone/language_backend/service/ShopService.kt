package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ShopService(private val userRepository: UserRepository) {

    fun buyItem(userId: UUID, itemType: String) {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("Usuario no encontrado") }

        when (itemType) {
            "HEART_REFILL" -> {
                if (user.heartsCount >= 5) throw RuntimeException("Ya tienes las vidas completas.")
                if (user.lingotsCount < 50) throw RuntimeException("No tienes suficientes gemas.")

                user.lingotsCount -= 50
                user.heartsCount = 5 // Rellenar a 5 según el estándar de tu UI
            }
            "STREAK_FREEZE" -> {
                if (user.hasStreakFreeze) throw RuntimeException("Ya tienes un protector de racha.")
                if (user.lingotsCount < 200) throw RuntimeException("No tienes suficientes gemas.")

                user.lingotsCount -= 200
                user.hasStreakFreeze = true
            }
            "DOUBLE_OR_NOTHING" -> {
                if (user.lingotsCount < 50) throw RuntimeException("No tienes suficientes gemas.")
                user.lingotsCount -= 50
            }
            else -> throw RuntimeException("Ítem desconocido")
        }

        userRepository.save(user)
    }
}