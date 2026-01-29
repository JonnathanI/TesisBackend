package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.EvaluationRequest
import com.duolingo.clone.language_backend.entity.EvaluationEntity
import com.duolingo.clone.language_backend.entity.QuestionEntity
import com.duolingo.clone.language_backend.repository.EvaluationRepository
import com.duolingo.clone.language_backend.repository.QuestionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.*
import java.math.BigDecimal

@Service
class EvaluationService(
    private val evaluationRepository: EvaluationRepository,
    private val questionRepository: QuestionRepository,
    private val questionTypeRepository: com.duolingo.clone.language_backend.repository.QuestionTypeRepository
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
            val type = questionTypeRepository.findById(qDto.questionTypeId)
                .orElseThrow { RuntimeException("Tipo no encontrado") }

            val newQuestion = QuestionEntity(
                lesson = null,            // No tiene lección
                evaluation = savedEvaluation, // ASOCIACIÓN CORRECTA
                questionType = type,
                textSource = qDto.textSource,
                textTarget = qDto.textTarget,
                options = qDto.options,
                category = "EVALUATION",  // Marcamos que es de examen
                active = true,
                difficultyScore = BigDecimal("1.0") // Valor por defecto
            )
            questionRepository.save(newQuestion)
        }

        return savedEvaluation
    }

    fun findAll(): List<EvaluationEntity> {
        return evaluationRepository.findAll()
    }
}