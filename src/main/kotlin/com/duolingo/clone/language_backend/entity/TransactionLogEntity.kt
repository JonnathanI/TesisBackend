package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "transaction_log")
data class TransactionLogEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(nullable = false)
    val type: String, // Ej: 'LESSON_COMPLETED', 'HEART_BOUGHT'

    @Column(nullable = false)
    val amount: Int, // Valor positivo o negativo

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now()
)