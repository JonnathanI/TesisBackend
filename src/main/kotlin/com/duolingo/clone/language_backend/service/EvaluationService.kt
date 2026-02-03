package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.EvaluationRequest
import com.duolingo.clone.language_backend.entity.EvaluationEntity
import com.duolingo.clone.language_backend.entity.QuestionEntity
import com.duolingo.clone.language_backend.repository.EvaluationRepository
import com.duolingo.clone.language_backend.repository.QuestionRepository
import com.duolingo.clone.language_backend.repository.QuestionTypeRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class EvaluationService(
    private val evaluationRepository: EvaluationRepository,
    private val questionRepository: QuestionRepository,
    private val questionTypeRepository: QuestionTypeRepository
) {

    @Transactional
    fun createEvaluation(request: EvaluationRequest): EvaluationEntity {
        // 1) Crear y guardar la Evaluación
        val evaluation = EvaluationEntity(
            title = request.title,
            description = request.description
        )
        val savedEvaluation = evaluationRepository.save(evaluation)

        // 2) Crear las preguntas vinculadas a esa evaluación
        request.questions.forEach { qDto ->

            // Aseguramos que el tipo existe
            val type = questionTypeRepository.findById(qDto.questionTypeId)
                .orElseThrow { RuntimeException("Tipo de pregunta no encontrado: ${qDto.questionTypeId}") }

            val question = QuestionEntity(
                lesson = null,                       // 👈 en evaluación no hay lección
                evaluation = savedEvaluation,        // 👈 se asocia a la evaluación
                questionType = type,
                textSource = qDto.textSource,
                textTarget = qDto.textTarget,
                options = qDto.options,              // 👉 aquí van las opciones (pueden ser JSON con imageUrl)
                audioUrl = qDto.audioUrl,           // 🔊 URL del audio (si la estás mandando)
                category = qDto.category ?: "EVALUATION",
                active = true,
                difficultyScore = BigDecimal.valueOf(qDto.difficultyScore ?: 1.0)
            )

            questionRepository.save(question)
        }

        return savedEvaluation
    }

    fun findAll(): List<EvaluationEntity> =
        evaluationRepository.findAll()
}
