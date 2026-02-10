package com.duolingo.clone.language_backend.entity

import com.fasterxml.jackson.annotation.JsonIgnore // 1. IMPORTANTE: Añade este import
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "classroom")
data class ClassroomEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val code: String,

    // 🔹 RELACIÓN CON EL CURSO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    var course: CourseEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnore // 2. IMPORTANTE: Añade esta anotación
    val teacher: UserEntity,

    @ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "classroom_students",
    joinColumns = [JoinColumn(name = "classroom_id")],
    inverseJoinColumns = [JoinColumn(name = "student_id")]
)
@JsonIgnore // Ignorar para evitar ciclos infinitos al listar

val students: MutableList<UserEntity> = mutableListOf(),
    @OneToMany(mappedBy = "classroom", cascade = [CascadeType.ALL], orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    val assignments: MutableList<AssignmentEntity> = mutableListOf()
)