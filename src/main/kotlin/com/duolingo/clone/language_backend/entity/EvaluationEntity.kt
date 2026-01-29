package com.duolingo.clone.language_backend.entity

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "evaluations")
class EvaluationEntity(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    var title: String,
    var description: String? = null,

    // Una evaluación tiene muchas preguntas (relación 1 a N)
    @OneToMany(mappedBy = "evaluation", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonManagedReference(value = "evaluation-questions")
    var questions: MutableList<QuestionEntity> = mutableListOf(),

    var active: Boolean = true
)