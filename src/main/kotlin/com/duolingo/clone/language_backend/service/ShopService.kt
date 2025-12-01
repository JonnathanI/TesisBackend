package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.entity.TransactionLog
import com.duolingo.clone.language_backend.entity.TransactionType
import com.duolingo.clone.language_backend.entity.UserEntity
import com.duolingo.clone.language_backend.repository.TransactionLogRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*
import kotlin.NoSuchElementException

@Service
class ShopService(
    private val userRepository: UserRepository,
    private val transactionLogRepository: TransactionLogRepository
) {
    // Precios de ejemplo
    private val HEART_REFILL_COST = 450
    private val STREAK_FREEZE_COST = 200

    fun buyItem(userId: UUID, itemType: TransactionType): UserEntity {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("Usuario no encontrado.") }

        val cost = when (itemType) {
            TransactionType.HEART_BOUGHT -> HEART_REFILL_COST
            TransactionType.STREAK_FREEZE_BOUGHT -> STREAK_FREEZE_COST
            // Si el enum tiene otros valores (ej: LESSON_COMPLETED), debes manejarlos:
            // TransactionType.LESSON_COMPLETED -> throw IllegalArgumentException("No se puede comprar una lección completada.")
            else -> throw IllegalArgumentException("Tipo de artículo no válido para compra.")
        }

        if (user.lingotsCount < cost) {
            throw IllegalArgumentException("Lingots insuficientes.")
        }

        // 1. Deducir costo
        user.lingotsCount -= cost

        // 2. Aplicar beneficio
        if (itemType == TransactionType.HEART_BOUGHT) {
            user.heartsCount = 5 // Recarga instantánea
        } else if (itemType == TransactionType.STREAK_FREEZE_BOUGHT) {
            user.hasStreakFreeze = true
        }

        // 3. Registrar transacción (Monto negativo)
        val log = TransactionLog(
            user = user,
            // CORRECCIÓN: Usar .name para convertir el Enum a String
            type = itemType,
            amount = -cost // Gasto es negativo
        )
        transactionLogRepository.save(log)

        return userRepository.save(user)
    }
}