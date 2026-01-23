package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

// --- ¡CAMBIO IMPORTANTE! ---
// 1. Le decimos a JPA que esta entidad usa una clave compuesta definida en 'UserLessonProgressId'
@IdClass(UserLessonProgressId::class)
@Entity
@Table(name = "user_lesson_progress")
data class UserLessonProgressEntity(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    val lesson: LessonEntity,

    @Column(name = "is_completed", nullable = false)
    var isCompleted: Boolean = false,

    // ✅ CAMBIO CLAVE: Agregamos el signo '?' para que sea nullable en Kotlin
    @Column(name = "correct_answers", nullable = true)
    var correctAnswers: Int? = 0,

    @Column(name = "mistakes_count", nullable = true)
    var mistakesCount: Int? = 0,

    @Column(name = "last_practiced")
    var lastPracticed: Instant? = null,

    @Column(name = "mastery_level", nullable = false)
    var masteryLevel: Int = 0
)