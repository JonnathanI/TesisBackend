// src/main/kotlin/com/duolingo/clone/language_backend/entity/FcmTokenEntity.kt
package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "fcm_token")
data class FcmTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(nullable = false, unique = true, length = 300)
    var token: String,

    @Column(nullable = false)
    var active: Boolean = true
)