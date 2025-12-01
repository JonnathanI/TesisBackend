package com.duolingo.clone.language_backend.dto

import java.math.BigDecimal
import java.util.UUID

data class QuestionRequest(
    val lessonId: UUID,
    val questionTypeId: String, // Ejemplo: 'TRANSLATION_TO_TARGET'
    val textSource: String,
    val textTarget: String? = null,

    // --- ¡CAMPO AÑADIDO! ---
    val options: List<String> = emptyList(), // <-- AÑADE ESTA LÍNEA

    val audioUrl: String? = null,
    val hintJson: String? = null,
    val difficultyScore: BigDecimal = BigDecimal("0.5")
)