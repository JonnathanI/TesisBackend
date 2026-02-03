package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.EvaluationRequest
import com.duolingo.clone.language_backend.entity.EvaluationAssignmentEntity
import com.duolingo.clone.language_backend.entity.EvaluationEntity
import com.duolingo.clone.language_backend.repository.EvaluationAssignmentRepository // 👈 Importa el nuevo
import com.duolingo.clone.language_backend.repository.ClassroomRepository
import com.duolingo.clone.language_backend.repository.EvaluationRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import com.duolingo.clone.language_backend.service.CloudinaryService
import com.duolingo.clone.language_backend.service.EvaluationService
import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api/teacher/evaluations")
class EvaluationController(
    private val evaluationService: EvaluationService,
    private val evaluationRepository: EvaluationRepository,
    private val classroomRepository: ClassroomRepository,
    // CAMBIO CRÍTICO: Usar el repositorio específico para evaluaciones
    private val assignmentRepository: EvaluationAssignmentRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService
) {
    @PostMapping
    fun createFullEvaluation(@RequestBody request: EvaluationRequest): ResponseEntity<EvaluationEntity> {
        val saved = evaluationService.createEvaluation(request)
        return ResponseEntity.ok(saved)
    }

    @GetMapping
    fun getMyEvaluations(): ResponseEntity<List<EvaluationEntity>> {
        return ResponseEntity.ok(evaluationRepository.findAll())
    }

    // Ahora findByStudentIdAndCompletedFalse funcionará porque el repo es el correcto
    @GetMapping("/pending")
    fun getMyPendingEvaluations(@RequestParam studentId: UUID): ResponseEntity<List<EvaluationAssignmentEntity>> {
        return ResponseEntity.ok(assignmentRepository.findByStudentIdAndCompletedFalse(studentId))
    }

    @PostMapping("/{evaluationId}/assign/{classroomId}")
    @Transactional
    fun assignToClassroom(
        @PathVariable evaluationId: UUID,
        @PathVariable classroomId: UUID
    ): ResponseEntity<String> {
        val evaluation = evaluationRepository.findById(evaluationId)
            .orElseThrow { RuntimeException("Evaluación no encontrada") }

        val classroom = classroomRepository.findById(classroomId)
            .orElseThrow { RuntimeException("Aula no encontrada") }

        val students = classroom.students

        val assignments = students.map { student ->
            EvaluationAssignmentEntity(
                evaluation = evaluation,
                student = student,
                dueDate = LocalDateTime.now().plusDays(7)
            )
        }

        // Ya no habrá "Type mismatch" aquí
        assignmentRepository.saveAll(assignments)

        return ResponseEntity.ok("Asignado correctamente a ${students.size} alumnos")
    }

    @PostMapping("/{evaluationId}/assign-student/{studentId}")
    @Transactional
    fun assignToStudent(
        @PathVariable evaluationId: UUID,
        @PathVariable studentId: UUID
    ): ResponseEntity<String> {
        val evaluation = evaluationRepository.findById(evaluationId)
            .orElseThrow { RuntimeException("Evaluación no encontrada") }

        // Buscamos al usuario (puedes usar tu UserRepository)
        val student = userRepository.findById(studentId)
            .orElseThrow { RuntimeException("Estudiante no encontrado") }

        val assignment = EvaluationAssignmentEntity(
            evaluation = evaluation,
            student = student,
            dueDate = LocalDateTime.now().plusDays(7)
        )

        assignmentRepository.save(assignment)

        return ResponseEntity.ok("Evaluación asignada correctamente a ${student.fullName}")
    }


    @GetMapping("/assignment/{assignmentId}")
    fun getAssignmentDetails(@PathVariable assignmentId: UUID): ResponseEntity<EvaluationAssignmentEntity> {
        val assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow { RuntimeException("Asignación no encontrada") }
        return ResponseEntity.ok(assignment)
    }

    @PostMapping("/upload")
    fun uploadGenericFile(
        @RequestParam file: MultipartFile,
        @RequestParam(required = false, defaultValue = "misc") folder: String
    ): ResponseEntity<String> {
        val url = cloudinaryService.uploadFile(file, folder)
        return ResponseEntity.ok(url) // 👈 Devuelves solo la URL
    }

}