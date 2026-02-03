package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.EvaluationPlayerDTO
import com.duolingo.clone.language_backend.dto.EvaluationQuestionDTO
import com.duolingo.clone.language_backend.dto.PendingEvaluationDTO
import com.duolingo.clone.language_backend.repository.EvaluationAssignmentRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/student/evaluations")
class StudentEvaluationController(
    private val assignmentRepository: EvaluationAssignmentRepository
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

        val dto = EvaluationPlayerDTO(
            id = evaluation.id!!,
            title = evaluation.title,
            description = evaluation.description,
            questions = evaluation.questions.map { q ->
                EvaluationQuestionDTO(
                    id = q.id!!,
                    textSource = q.textSource,
                    textTarget = q.textTarget,
                    options = q.options,
                    questionType = q.questionType.typeName
                )
            }
        )

        return ResponseEntity.ok(dto)
    }

    // 👇 NUEVO: marcar la evaluación como completada
    data class CompleteEvaluationRequest(
        val score: Int? = null,          // el front manda un número
        val status: String? = null       // lo aceptamos pero NO lo usamos por ahora
    )

    @PostMapping("/assignment/{assignmentId}/complete")
    fun completeEvaluation(
        @PathVariable assignmentId: UUID,
        @RequestBody body: CompleteEvaluationRequest
    ): ResponseEntity<String> {

        val assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow { RuntimeException("Asignación no encontrada") }

        assignment.completed = true
        // 👇 convertimos Int → BigDecimal para encajar con la entidad
        assignment.score = (body.score ?: 0).toBigDecimal()

        assignmentRepository.save(assignment)

        return ResponseEntity.ok("Evaluación completada correctamente")
    }

    @GetMapping("/all")   // 🔹 NUEVO
    fun getAllEvaluations(
        @RequestParam studentId: UUID
    ): ResponseEntity<List<PendingEvaluationDTO>> =
        ResponseEntity.ok(
            assignmentRepository.findAllEvaluationsForStudent(studentId)
        )
}
