package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "registration_code")
data class RegistrationCodeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(unique = true, nullable = false)
    val code: String,
/*
    @Column(nullable = false)
    var isUsed: Boolean = false,*/

    @Column(nullable = false)
    val expiresAt: Instant,

    // 🎯 Cupos máximos
    @Column(nullable = false)
    val maxUses: Int,


    // 📉 Usos actuales
    @Column(nullable = false)
    var usedCount: Int = 0,

    // Profesor que generó el código
    @Column(name = "created_by_teacher_id", nullable = true)
    val createdByTeacherId: UUID? = null,

    // Opcional: Relacionar con un curso específico
    @Column(name = "course_id")
    val courseId: UUID? = null
)