package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class QuestionRequest(
    var lessonId: UUID? = null,
    var questionTypeId: UUID? = null, // Cambiado a opcional para evitar crash si llega nulo
    var textSource: String = "",
    var textTarget: String? = null,
    var options: List<String> = emptyList(),
    var audioUrl: String? = null,
    var hintJson: String? = null,
    var difficultyScore: Double = 0.5, // Double es más amigable para formularios
    var active: Boolean = true,
)