package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.EvaluationPlayerDTO
import com.duolingo.clone.language_backend.dto.EvaluationQuestionDTO
import com.duolingo.clone.language_backend.dto.PendingEvaluationDTO
import com.duolingo.clone.language_backend.entity.EvaluationAnswerEntity
import com.duolingo.clone.language_backend.repository.EvaluationAnswerRepository
import com.duolingo.clone.language_backend.repository.EvaluationAssignmentRepository
import com.duolingo.clone.language_backend.repository.QuestionRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/student/evaluations")
class StudentEvaluationController(
    private val assignmentRepository: EvaluationAssignmentRepository,
    private val evaluationAnswerRepository: EvaluationAnswerRepository,
    private val questionRepository: QuestionRepository
) {

    @GetMapping("/pending")
    fun getPendingEvaluations(
        @RequestParam studentId: UUID
    ): ResponseEntity<List<PendingEvaluationDTO>> {
        return ResponseEntity.ok(
            assignmentRepository.findPendingEvaluationsForStudent(studentId)
        )
    }

    @GetMapping("/assignment/{assignmentId}")
    fun getEvaluationByAssignment(
        @PathVariable assignmentId: UUID
    ): ResponseEntity<EvaluationPlayerDTO> {

        val assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow { RuntimeException("Asignación no encontrada") }

        val evaluation = assignment.evaluation
            ?: throw RuntimeException("La asignación no tiene evaluación")

        // 🔎 Traemos todas las respuestas que el estudiante guardó para esta asignación
        val answers = evaluationAnswerRepository.findByAssignmentId(assignmentId)

        val dto = EvaluationPlayerDTO(
            id = evaluation.id!!,
            title = evaluation.title,
            description = evaluation.description,
            completed = assignment.completed,   // 👈 importante
            score = assignment.score,
            questions = evaluation.questions.map { q ->
                val answerEntity = answers.find { it.question.id == q.id }

                EvaluationQuestionDTO(
                    id = q.id!!,
                    textSource = q.textSource,
                    textTarget = q.textTarget,
                    options = q.options,
                    questionType = q.questionType.typeName,
                    studentAnswer = answerEntity?.answerText,
                    isCorrect = answerEntity?.isCorrect
                )
            }
        )

        return ResponseEntity.ok(dto)
    }


    // ---------- DTOs para completar evaluación ----------

    data class AnswerSubmitDTO(
        val questionId: UUID,
        val answerText: String
    )

    data class CompleteEvaluationRequest(
        val score: Int? = null,                  // el front manda número
        val status: String? = null,              // por si luego quieres usarlo
        val answers: List<AnswerSubmitDTO> = emptyList() // 👈 para guardar respuestas
    )

    // ---------- Marcar evaluación como completa ----------

    @PostMapping("/assign/{assignmentId}/complete")
    fun completeEvaluation(
        @PathVariable assignmentId: UUID,
        @RequestBody req: CompleteEvaluationRequest
    ): ResponseEntity<String> {

        val assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow { RuntimeException("Asignación no encontrada") }

        // 1) marcar puntaje y completado
        assignment.completed = true
        assignment.score = req.score?.let { BigDecimal.valueOf(it.toLong()) }  // ✅ Int? → BigDecimal?
        assignmentRepository.save(assignment)

        // 2) guardar respuestas (por ahora, si el front no las manda, será lista vacía)
        req.answers.forEach { ansDto ->
            val question = questionRepository.findById(ansDto.questionId)
                .orElseThrow { RuntimeException("Pregunta no encontrada: ${ansDto.questionId}") }

            val correct = question.textTarget
                ?.trim()
                ?.equals(ansDto.answerText.trim(), ignoreCase = true)
                ?: false

            val answerEntity = EvaluationAnswerEntity(
                assignment = assignment,
                question = question,
                answerText = ansDto.answerText,
                isCorrect = correct
            )

            evaluationAnswerRepository.save(answerEntity)
        }

        return ResponseEntity.ok("Evaluación completada y respuestas guardadas")
    }

    @GetMapping("/all")
    fun getAllEvaluations(
        @RequestParam studentId: UUID
    ): ResponseEntity<List<PendingEvaluationDTO>> =
        ResponseEntity.ok(
            assignmentRepository.findAllEvaluationsForStudent(studentId)
        )
}
