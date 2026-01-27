package com.duolingo.clone.language_backend.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "question")
class QuestionEntity( // Quitamos "data"
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    @JsonBackReference
    var lesson: LessonEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_type_id", nullable = false)
    var questionType: QuestionTypeEntity,

    @Column(name = "text_source", nullable = false)
    var textSource: String,

    @Column(name = "text_target")
    var textTarget: String? = null,

    @Type(JsonType::class)
    @Column(name = "options", columnDefinition = "jsonb")
    var options: List<String> = emptyList(),

    @Column(name = "audio_url")
    var audioUrl: String? = null,

    @Type(JsonType::class)
    @Column(name = "hint_json", columnDefinition = "jsonb")
    var hintJson: String? = null,

    @Column(name = "category", nullable = false)
    var category: String = "GRAMMAR",

    @Column(name = "active")
    var active: Boolean = true,

    @Column(name = "difficulty_score", nullable = false)
    var difficultyScore: BigDecimal = BigDecimal("1.0")
)