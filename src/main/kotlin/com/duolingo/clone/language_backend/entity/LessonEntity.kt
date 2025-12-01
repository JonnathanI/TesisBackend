package com.duolingo.clone.language_backend.entity

import com.fasterxml.jackson.annotation.JsonBackReference // <-- AÑADIR
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "lesson")
data class LessonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    // Detiene la serialización en esta dirección
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    @JsonManagedReference
    val unit: UnitEntity,

    @Column(nullable = false)
    val title: String,

    @Column(name = "lesson_order", nullable = false)
    val lessonOrder: Int,

    @Column(name = "required_xp", nullable = false)
    val requiredXp: Int,

    // Esta es la referencia "Managed" para la relación Lesson ↔ Question (si existe)
    @OneToMany(mappedBy = "lesson", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val questions: List<QuestionEntity> = emptyList()
)