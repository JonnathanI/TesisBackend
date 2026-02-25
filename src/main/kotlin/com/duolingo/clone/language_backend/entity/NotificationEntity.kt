package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

enum class NotificationType {
    TASK_ASSIGNED,
    EVALUATION_ASSIGNED,
    MESSAGE_RECEIVED,
    FRIEND_REQUEST
}

@Entity
@Table(name = "notification")
data class NotificationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,                 // el usuario que recibe la notificación

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    @Column(nullable = false)
    val title: String,                    // "Nueva tarea de Grammar"

    @Column(nullable = false, length = 500)
    val message: String,                  // "Tu profesor Juan te asignó la tarea 1"

    @Column(name = "related_id")
    val relatedId: String? = null,        // id de tarea, evaluación, chat, etc.

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var read: Boolean = false
)