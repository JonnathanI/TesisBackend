package com.duolingo.clone.language_backend.entity

import com.fasterxml.jackson.annotation.JsonManagedReference // <-- IMPORTANTE
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "course")
data class CourseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val title: String,

    @Column(name = "target_language", nullable = false)
    val targetLanguage: String,

    @Column(name = "base_language", nullable = false)
    val baseLanguage: String,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    // Lista de unidades: Esta es la referencia "principal" (Managed Reference).
    // Serializará las unidades, pero las unidades no serializarán de vuelta al curso.
    @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference // <-- ANOTACIÓN AÑADIDA
    val units: List<UnitEntity> = emptyList() // Es vital inicializar colecciones
)