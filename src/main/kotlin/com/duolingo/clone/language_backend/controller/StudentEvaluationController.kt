package com.duolingo.clone.language_backend.controller

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
}
