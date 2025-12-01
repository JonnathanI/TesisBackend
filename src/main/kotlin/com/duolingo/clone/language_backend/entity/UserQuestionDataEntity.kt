package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.Instant // ¡NECESARIO! para lastPracticed

@Entity
@Table(name = "user_question_data")
@IdClass(UserQuestionDataId::class)
data class UserQuestionDataEntity(

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity, // El usuario no cambia, es 'val'

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: QuestionEntity, // La pregunta no cambia, es 'val'

    // --- CAMPOS QUE DEBEN SER MUTABLES (var) Y SON ACTUALIZADOS POR EL SR ---

    @Column(name = "strength_score", nullable = false)
    var strengthScore: BigDecimal = BigDecimal("0.5"), // CAMBIAR a 'var'

    @Column(name = "mastery_level", nullable = false) // ¡CAMPO FALTANTE AÑADIDO!
    var masteryLevel: Int = 0, // CAMBIAR a 'var'

    @Column(name = "last_practiced", nullable = false) // ¡CAMPO FALTANTE AÑADIDO!
    var lastPracticed: Instant, // CAMBIAR a 'var'

    @Column(name = "next_due_date", nullable = false)
    var nextDueDate: LocalDateTime // CAMBIAR a 'var' (y quitar el '?' para no ser anulable si lo gestionamos)
)