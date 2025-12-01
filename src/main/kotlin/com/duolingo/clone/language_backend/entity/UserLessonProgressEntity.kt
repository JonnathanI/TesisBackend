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

    // --- ¡CAMBIO IMPORTANTE! ---
    // 2. 'id: UUID' se elimina.
    // En su lugar, marcamos 'user' y 'lesson' como las claves primarias.
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    val lesson: LessonEntity,

    // ... el resto de tus campos están bien ...
    @Column(name = "is_completed", nullable = false)
    var isCompleted: Boolean = false,

    @Column(name = "last_practiced")
    var lastPracticed: Instant? = null,

    @Column(name = "mastery_level", nullable = false)
    var masteryLevel: Int = 0
)