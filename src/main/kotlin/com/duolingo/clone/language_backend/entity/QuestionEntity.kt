package com.duolingo.clone.language_backend.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import io.hypersistence.utils.hibernate.type.json.JsonType // <-- Asegúrate que este import esté
import jakarta.persistence.*
import org.hibernate.annotations.Type // <-- Asegúrate que este import esté
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
    val textSource: String, // Ejemplo: "Manzana"

    @Column(name = "text_target")
    val textTarget: String? = null, // La respuesta correcta, ej: "Apple"

    // Este ya lo arreglamos antes
    @Type(JsonType::class)
    @Column(name = "options", columnDefinition = "jsonb")
    val options: List<String> = emptyList(),

    @Column(name = "audio_url")
    val audio_url: String? = null,

    // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
    @Type(JsonType::class) // <-- AÑADE ESTA LÍNEA
    @Column(name = "hint_json", columnDefinition = "jsonb")
    val hintJson: String? = null,
    // --- ---

    @Column(name = "difficulty_score", nullable = false)
    val difficultyScore: BigDecimal
)