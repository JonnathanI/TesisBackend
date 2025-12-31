package com.duolingo.clone.language_backend.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "question")
data class QuestionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    @JsonBackReference
    val lesson: LessonEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_type_id", nullable = false)
    val questionType: QuestionTypeEntity,

    @Column(name = "text_source", nullable = false)
    val textSource: String,

    @Column(name = "text_target")
    val textTarget: String? = null,

    @Type(JsonType::class)
    @Column(name = "options", columnDefinition = "jsonb")
    val options: List<String> = emptyList(),

    @Column(name = "audio_url")
    val audioUrl: String? = null, // Cambiado a camelCase (estándar Kotlin)

    @Type(JsonType::class)
    @Column(name = "hint_json", columnDefinition = "jsonb")
    val hintJson: String? = null,

    // --- NUEVO CAMPO: CATEGORÍA ---
    // Valores sugeridos: "GRAMMAR", "LISTENING", "SPEAKING", "VOCABULARY"
    @Column(name = "category", nullable = false)
    val category: String = "GRAMMAR",

    @Column(name = "difficulty_score", nullable = false)
    val difficultyScore: BigDecimal = BigDecimal("1.0")
)