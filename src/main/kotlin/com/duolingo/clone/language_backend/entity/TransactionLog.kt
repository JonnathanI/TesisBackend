package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "transaction_log")
data class TransactionLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    val type: TransactionType, // Ej: LESSON_COMPLETED, HEART_BOUGHT

    @Column(name = "amount", nullable = false)
    val amount: Int, // Positivo para ganancia, Negativo para gasto

    @Column(name = "timestamp", nullable = false)
    val timestamp: Instant = Instant.now()
)

enum class TransactionType {
    LESSON_COMPLETED, // Ganancia
    STREAK_FREEZE_BOUGHT, // Gasto
    HEART_BOUGHT, // Gasto
    DAILY_BONUS // Ganancia
}