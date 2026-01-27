package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "user_relationships")
data class UserRelationship(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne @JoinColumn(name = "follower_id")
    val follower: UserEntity,

    @ManyToOne @JoinColumn(name = "followed_id")
    val followed: UserEntity,

    // PENDING: Solicitud enviada, ACCEPTED: Amistad confirmada
    @Column(nullable = false)
    var status: String = "PENDING",

    val createdAt: Instant = Instant.now()
)