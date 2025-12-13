package com.duolingo.clone.language_backend.entity

import com.fasterxml.jackson.annotation.JsonBackReference // <--- IMPORTANTE: Agrega esto
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "lesson")
data class LessonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    @JsonBackReference // <--- ¡ESTA LÍNEA DETIENE EL BUCLE!
    val unit: UnitEntity,

    @Column(nullable = false)
    val title: String,

    @Column(name = "lesson_order", nullable = false)
    val lessonOrder: Int,

    @Column(name = "required_xp", nullable = false)
    val requiredXp: Int,

    @OneToMany(mappedBy = "lesson", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val questions: List<QuestionEntity> = emptyList()
)