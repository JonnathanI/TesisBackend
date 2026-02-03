package com.duolingo.clone.language_backend.dto

import java.util.UUID

data class EvaluationQuestionDTO(
    val id: UUID,
    val textSource: String,
    val textTarget: String?,
    val options: List<String>,

    // nombre del tipo, por ejemplo "LISTENING", "IMAGE_SELECT"
    val questionType: String,

    // 🔊 NUEVO: si no hay audio, viene null
    val audioUrl: String? = null,

    // 🖼️ NUEVO: si no hay imágenes, lista vacía
    val imageUrls: List<String> = emptyList()
)
