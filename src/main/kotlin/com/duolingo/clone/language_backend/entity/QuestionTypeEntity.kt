package com.duolingo.clone.language_backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "question_type")
data class QuestionTypeEntity(
    @Id
    val id: String, // Ejemplo: 'TRANSLATION_TO_TARGET'

    @Column
    val description: String? = null
)