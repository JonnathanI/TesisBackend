package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "daily_sql_challenge")
data class DailySqlChallengeEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val snippet: String
)