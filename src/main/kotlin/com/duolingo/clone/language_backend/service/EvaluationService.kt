package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.EvaluationRequest
import com.duolingo.clone.language_backend.entity.EvaluationEntity
import com.duolingo.clone.language_backend.entity.NotificationType
import com.duolingo.clone.language_backend.entity.QuestionEntity
import com.duolingo.clone.language_backend.repository.ClassroomRepository
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
    private val questionTypeRepository: QuestionTypeRepository,
    private val classroomRepository: ClassroomRepository,      // 👈 IMPORTANTE
    private val notificationService: NotificationService       // 👈 IMPORTANTE
) {

    @Transactional
    fun createEvaluation(request: EvaluationRequest): EvaluationEntity {
        // 1️⃣ Crear la evaluación
        val evaluation = EvaluationEntity(
            title = request.title,
            description = request.description
        )
        val savedEvaluation = evaluationRepository.save(evaluation)

        // 2️⃣ Crear preguntas
        request.questions.forEach { qDto ->
            val type = questionTypeRepository.findById(qDto.questionTypeId)
                .orElseThrow { RuntimeException("Tipo de pregunta no encontrado: ${qDto.questionTypeId}") }

            val question = QuestionEntity(
                lesson = null,
                evaluation = savedEvaluation,
                questionType = type,
                textSource = qDto.textSource,
                textTarget = qDto.textTarget,
                options = qDto.options,
                audioUrl = qDto.audioUrl,
                category = qDto.category ?: "EVALUATION",
                active = true,
                difficultyScore = BigDecimal.valueOf(qDto.difficultyScore ?: 1.0)
            )

            questionRepository.save(question)
        }

        // 3️⃣ Si viene classroomId -> notificar a los estudiantes del aula
        if (request.classroomId != null) {
            val classroom = classroomRepository.findById(request.classroomId)
                .orElseThrow { RuntimeException("Clase no encontrada") }

            classroom.students.forEach { student ->
                notificationService.createNotification(
                    user = student,
                    type = NotificationType.EVALUATION_ASSIGNED,  // 👈 Usamos TU enum
                    title = "Nueva evaluación: ${savedEvaluation.title}",
                    message = "Se ha publicado una nueva evaluación en el grupo ${classroom.name}.",
                    relatedId = savedEvaluation.id.toString()
                )
            }
        }

        return savedEvaluation
    }

    fun findAll(): List<EvaluationEntity> =
        evaluationRepository.findAll()
}