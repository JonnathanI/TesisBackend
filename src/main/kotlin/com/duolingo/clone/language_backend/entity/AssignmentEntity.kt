package com.duolingo.clone.language_backend.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.util.UUID
import java.time.LocalDate

@Entity
@Table(name = "assignment")
data class AssignmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val description: String,

    @Column(name = "xp_reward", nullable = false)
    val xpReward: Int,

    @Column(name = "due_date")
    val dueDate: LocalDate? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    @JsonBackReference
    val classroom: ClassroomEntity
)