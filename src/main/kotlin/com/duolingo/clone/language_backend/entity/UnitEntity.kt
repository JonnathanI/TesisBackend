package com.duolingo.clone.language_backend.entity

import com.fasterxml.jackson.annotation.JsonManagedReference // <--- IMPORTANTE: Agrega esto
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "unit")
data class UnitEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    val course: CourseEntity,

    @Column(nullable = false)
    val title: String,

    @Column(name = "unit_order", nullable = false)
    val unitOrder: Int,

    // Relación con las lecciones
    @OneToMany(mappedBy = "unit", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonManagedReference // <--- ¡ESTA LÍNEA ES LA CLAVE!
    val lessons: List<LessonEntity> = emptyList()
)