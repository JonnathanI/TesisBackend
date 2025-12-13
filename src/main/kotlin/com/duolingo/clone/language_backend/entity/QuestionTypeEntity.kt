package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "question_type")
data class QuestionTypeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    // --- CORRECCIÓN AQUÍ ---
    // El campo DEBE llamarse 'typeName' para que el repositorio funcione
    @Column(name = "type_name", nullable = false, unique = true)
    val typeName: String,

    @Column(name = "description")
    val description: String? = null
)