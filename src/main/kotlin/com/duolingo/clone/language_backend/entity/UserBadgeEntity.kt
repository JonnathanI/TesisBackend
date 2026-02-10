package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "user_badges")
data class UserBadgeEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    val badge: BadgeEntity,

    @Column(nullable = false)
    val earnedAt: Date = Date()    // 👈 NECESARIO
)
