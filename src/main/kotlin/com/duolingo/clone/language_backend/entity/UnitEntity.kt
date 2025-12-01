package com.duolingo.clone.language_backend.entity

import com.fasterxml.jackson.annotation.JsonBackReference // <-- IMPORTANTE
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "unit")
data class UnitEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    // Relación con Course: Esta es la referencia "secundaria" (Back Reference).
    // Jackson ignora esta propiedad al serializar UnitEntity para evitar el ciclo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonBackReference // <-- ANOTACIÓN AÑADIDA
    val course: CourseEntity,

    @Column(nullable = false)
    val title: String,

    @Column(name = "unit_order", nullable = false)
    val unitOrder: Int,

    // También debes añadir la referencia a Lecciones para el siguiente nivel del ciclo:
    @OneToMany(mappedBy = "unit", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    val lessons: List<LessonEntity> = emptyList() // Es vital inicializar colecciones
)