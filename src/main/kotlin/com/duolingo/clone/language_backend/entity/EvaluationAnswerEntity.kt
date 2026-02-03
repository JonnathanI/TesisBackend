package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "evaluation_answer")
class EvaluationAnswerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    var assignment: EvaluationAssignmentEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    var question: QuestionEntity,

    // 👉 aquí guardamos lo que el alumno respondió (texto libre)
    @Column(name = "answer_text", columnDefinition = "TEXT")
    var answerText: String? = null,

    // opcionalmente si quieres marcar si fue correcta
    @Column(name = "is_correct")
    var isCorrect: Boolean? = null,

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
)
