package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID
import io.hypersistence.utils.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
@Entity
@Table(name = "app_user")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Column(name = "full_name", nullable = false)
    val fullName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole,

    // --- CAMPOS DE GAMIFICACIÓN (Todos mutables 'var') ---

    @Column(name = "xp_total", nullable = false)
    var xpTotal: Int = 0,

    @Column(name = "current_streak", nullable = false)
    var currentStreak: Int = 0,

    @Column(name = "hearts_count", nullable = false)
    var heartsCount: Int = 5,

    @Column(name = "last_practice_date")
    var lastPracticeDate: Instant? = null,

    // ¡NUEVO!: Marca de tiempo para la lógica de recarga de corazones
    @Column(name = "last_heart_refill_time")
    var lastHeartRefillTime: Instant? = null,

    // --- CAMPOS DE AUDITORÍA ---

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "lingots_count", nullable = false) // ¡NUEVO CAMPO!
    var lingotsCount: Int = 100, // Empezar con algunos
    @Column(name = "has_streak_freeze", nullable = false) // ¡NUEVO CAMPO!
    var hasStreakFreeze: Boolean = false,

    @Type(JsonType::class)
    @Column(name = "avatar_data", columnDefinition = "jsonb")
    var avatarData: String? = null,
)

enum class UserRole {
    STUDENT,
    TEACHER,
    ADMIN
}