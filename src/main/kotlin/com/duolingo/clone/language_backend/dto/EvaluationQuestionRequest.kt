package com.duolingo.clone.language_backend.dto

import java.util.*

data class EvaluationQuestionRequest(
    val textSource: String,
    val textTarget: String?,           // respuesta correcta (si aplica)
    val questionTypeId: UUID,          // id del tipo de pregunta (LISTENING, IMAGE_SELECT, etc)
    val options: List<String> = emptyList(),

    // Extra para lógica de dificultad/categoría si quieres reutilizarlo
    val difficultyScore: Double? = 1.0,
    val category: String? = "GRAMMAR",

    // 🔊 NUEVO: URL de audio (Cloudinary)
    val audioUrl: String? = null,

    // 🖼️ NUEVO: URLs de imágenes (una por opción en IMAGE_SELECT, por ejemplo)
    val imageUrls: List<String> = emptyList()
)
