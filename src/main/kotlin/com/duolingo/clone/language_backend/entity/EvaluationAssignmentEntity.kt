package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "evaluation_assignment")
class EvaluationAssignmentEntity(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    var evaluation: EvaluationEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    var student: UserEntity,

    @Column(name = "assigned_at")
    var assignedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "due_date")
    var dueDate: LocalDateTime? = null,

    @Column(name = "completed")
    var completed: Boolean = false,

    @Column(name = "score")
    var score: BigDecimal? = null
)