package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.EvaluationRequest
import com.duolingo.clone.language_backend.entity.EvaluationEntity
import com.duolingo.clone.language_backend.entity.QuestionEntity
import com.duolingo.clone.language_backend.repository.EvaluationRepository
import com.duolingo.clone.language_backend.repository.QuestionRepository
import com.duolingo.clone.language_backend.repository.QuestionTypeRepository // Simplificado
import jakarta.transaction.Transactional
import org.springframework.stereotype.*
import java.math.BigDecimal
import java.util.UUID // IMPORTANTE: Asegúrate de tener esta importación

@Service
class EvaluationService(
    private val evaluationRepository: EvaluationRepository,
    private val questionRepository: QuestionRepository,
    private val questionTypeRepository: QuestionTypeRepository
) {
    @Transactional
    fun createEvaluation(request: EvaluationRequest): EvaluationEntity {
        // 1. Crear y guardar la Evaluación
        val evaluation = EvaluationEntity(
            title = request.title,
            description = request.description
        )
        val savedEvaluation = evaluationRepository.save(evaluation)

        // 2. Crear las preguntas vinculadas
        request.questions.forEach { qDto ->
            // CORRECCIÓN: Manejo del UUID? para evitar el Type Mismatch
            val typeId = qDto.questionTypeId ?: throw RuntimeException("El ID del tipo de pregunta es obligatorio")

            val type = questionTypeRepository.findById(typeId)
                .orElseThrow { RuntimeException("Tipo no encontrado con ID: $typeId") }

            val newQuestion = QuestionEntity(
                lesson = null,
                evaluation = savedEvaluation,
                questionType = type,
                textSource = qDto.textSource,
                textTarget = qDto.textTarget,
                options = qDto.options,
                category = "EVALUATION",
                active = true,
                difficultyScore = BigDecimal("1.0")
            )
            questionRepository.save(newQuestion)
        }

        return savedEvaluation
    }

    fun findAll(): List<EvaluationEntity> {
        return evaluationRepository.findAll()
    }
}