package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.EvaluationRequest
import com.duolingo.clone.language_backend.entity.EvaluationAssignmentEntity
import com.duolingo.clone.language_backend.entity.EvaluationEntity
import com.duolingo.clone.language_backend.entity.NotificationType
import com.duolingo.clone.language_backend.repository.EvaluationAssignmentRepository
import com.duolingo.clone.language_backend.repository.ClassroomRepository
import com.duolingo.clone.language_backend.repository.EvaluationRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import com.duolingo.clone.language_backend.service.CloudinaryService
import com.duolingo.clone.language_backend.service.EvaluationService
import com.duolingo.clone.language_backend.service.NotificationService   // 👈 IMPORTANTE
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
    private val assignmentRepository: EvaluationAssignmentRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService,
    private val notificationService: NotificationService               // 👈 INYECTAMOS EL SERVICE
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

    @GetMapping("/pending")
    fun getMyPendingEvaluations(@RequestParam studentId: UUID): ResponseEntity<List<EvaluationAssignmentEntity>> {
        return ResponseEntity.ok(assignmentRepository.findByStudentIdAndCompletedFalse(studentId))
    }

    // 🔹 ASIGNAR A UN AULA (CON NOTIFICACIÓN PUSH)
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

        assignmentRepository.saveAll(assignments)

        // 👇 AQUÍ DISPARAMOS NOTIFICACIÓN PARA CADA ALUMNO DEL AULA
        students.forEach { student ->
            notificationService.createNotification(
                user = student,
                type = NotificationType.EVALUATION_ASSIGNED,
                title = "Nueva evaluación: ${evaluation.title}",
                message = "Tienes una nueva evaluación en el grupo ${classroom.name}.",
                relatedId = evaluation.id.toString()
            )
        }

        return ResponseEntity.ok("Asignado correctamente a ${students.size} alumnos")
    }

    // 🔹 ASIGNAR A UN SOLO ALUMNO (CON NOTIFICACIÓN PUSH)
    @PostMapping("/{evaluationId}/assign-student/{studentId}")
    @Transactional
    fun assignToStudent(
        @PathVariable evaluationId: UUID,
        @PathVariable studentId: UUID
    ): ResponseEntity<String> {
        val evaluation = evaluationRepository.findById(evaluationId)
            .orElseThrow { RuntimeException("Evaluación no encontrada") }

        val student = userRepository.findById(studentId)
            .orElseThrow { RuntimeException("Estudiante no encontrado") }

        val assignment = EvaluationAssignmentEntity(
            evaluation = evaluation,
            student = student,
            dueDate = LocalDateTime.now().plusDays(7)
        )

        assignmentRepository.save(assignment)

        // 👇 AQUÍ DISPARAMOS NOTIFICACIÓN SOLO A ESE ALUMNO
        notificationService.createNotification(
            user = student,
            type = NotificationType.EVALUATION_ASSIGNED,
            title = "Nueva evaluación: ${evaluation.title}",
            message = "Tienes una nueva evaluación asignada por tu docente.",
            relatedId = evaluation.id.toString()
        )

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
        return ResponseEntity.ok(url)
    }
}