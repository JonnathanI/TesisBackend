package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "badges")
data class BadgeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(unique = true, nullable = false)
    val code: String,

    @Column(nullable = false)
    val title: String,            // 👈 NECESARIO

    @Column(nullable = false)
    val description: String       // 👈 NECESARIO
)
