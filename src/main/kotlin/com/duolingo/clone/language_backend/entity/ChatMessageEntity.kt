package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

// ChatMessageEntity.kt
@Entity
@Table(name = "chat_message")
data class ChatMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: UserEntity,

    @Column(nullable = false, length = 1000)
    val content: String,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    // 👇 NUEVO: URL del archivo en Cloudinary
    @Column(nullable = true)
    val attachmentUrl: String? = null,

    // 👇 NUEVO: tipo de archivo: IMAGE, VIDEO, AUDIO, FILE
    @Column(nullable = true, length = 20)
    val attachmentType: String? = null
)
