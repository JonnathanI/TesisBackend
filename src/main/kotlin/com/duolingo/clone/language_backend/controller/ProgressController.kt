package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.AnswerSubmission
import com.duolingo.clone.language_backend.dto.LessonProgressDTO
import com.duolingo.clone.language_backend.dto.PracticeAnswerSubmission // ¡NUEVO IMPORT!
import com.duolingo.clone.language_backend.dto.UnitStatusDTO
import com.duolingo.clone.language_backend.entity.QuestionEntity
import com.duolingo.clone.language_backend.entity.UserLessonProgressEntity
import com.duolingo.clone.language_backend.service.JwtService
import com.duolingo.clone.language_backend.service.ProgressService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/progress")
class ProgressController(
    private val progressService: ProgressService,
    private val jwtService: JwtService
) {
    // Nota: Todas estas rutas requieren autenticación (token JWT)

    /**
     * GET /api/progress/lessons/{lessonId}/questions
     * Devuelve las preguntas para iniciar un quiz.
     */
    @GetMapping("/lessons/{lessonId}/questions")
    fun getLessonQuestions(
        // Extrae el ID del usuario del token JWT
        @AuthenticationPrincipal userId: String,
        @PathVariable lessonId: UUID
    ): ResponseEntity<List<QuestionEntity>> {
        // No necesitamos el userUuid aquí, pero lo mantenemos para consistencia
        // val userUuid = UUID.fromString(userId)

        val questions = progressService.getQuestionsForLesson(lessonId)
        return ResponseEntity.ok(questions)
    }

    /**
     * POST /api/progress/submit
     * Envía una respuesta del estudiante para verificación.
     */
    @PostMapping("/submit")
    fun submitAnswer(
        @AuthenticationPrincipal userId: String,
        @RequestBody submission: AnswerSubmission
    ): ResponseEntity<AnswerSubmission> {

        val userUuid = UUID.fromString(userId)

        val result = progressService.submitAnswer(userUuid, submission)
        return ResponseEntity.ok(result)
    }

    /**
     * POST /api/progress/lessons/{lessonId}/complete
     * Finaliza la lección y actualiza el XP.
     */
    @PostMapping("/lessons/{lessonId}/complete")
    fun completeLesson(
        @AuthenticationPrincipal userId: String,
        @PathVariable lessonId: UUID,
        @RequestParam correct: Int,    // Número de respuestas correctas
        @RequestParam mistakes: Int    // ✅ AÑADIMOS ESTO: Número de fallos
    ): ResponseEntity<UserLessonProgressEntity> {

        val userUuid = UUID.fromString(userId)

        // Pasamos ambos valores al servicio
        val progress = progressService.completeLesson(userUuid, lessonId, correct, mistakes)
        return ResponseEntity.ok(progress)
    }

    @GetMapping("/units/{unitId}")
    fun getUnitProgress(
        @AuthenticationPrincipal userId: String,
        @PathVariable unitId: UUID
    ): ResponseEntity<List<LessonProgressDTO>> {

        val userUuid = UUID.fromString(userId)
        val progress = progressService.getUnitProgress(unitId, userUuid)

        return ResponseEntity.ok(progress)
    }


    @GetMapping("/practice/questions")
    fun getPracticeSession(request: HttpServletRequest): ResponseEntity<List<QuestionEntity>> {

        // 1. Extracción y Validación del Token (Reutilizando tu lógica)
        val authHeader = request.getHeader("Authorization")
        val token = if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else {
            return ResponseEntity.status(401).build() // No autorizado
        }

        // 2. Extraer el ID del usuario y convertir a UUID
        val userIdString = jwtService.extractUserId(token)
        val userId = try {
            UUID.fromString(userIdString)
        } catch (e: IllegalArgumentException) {
            // Manejar caso donde el ID en el token no es un UUID válido
            return ResponseEntity.status(400).build()
        }

        // 3. Llamar al servicio para obtener las preguntas
        val questions = progressService.getPracticeQuestions(userId)

        return ResponseEntity.ok(questions)
    }

    /**
     * POST /api/progress/practice/answer
     * Registra el resultado de una pregunta durante una sesión de práctica
     * y actualiza la lógica de Repaso Espaciado.
     */
    @PostMapping("/practice/answer")
    // 1. Usar 204 No Content (el más apropiado para POST sin cuerpo de retorno)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // 2. Simplificar la firma para usar @AuthenticationPrincipal (si tu filtro lo inyecta)
    fun submitPracticeAnswer(
        @AuthenticationPrincipal userId: String,
        @RequestBody submission: PracticeAnswerSubmission
    ) {
        // La lógica de extracción del ID es más limpia aquí
        val userUuid = UUID.fromString(userId)

        progressService.submitPracticeAnswer(userUuid, submission)

        // No se requiere 'return' ya que el @ResponseStatus maneja la respuesta.
    }

    @GetMapping("/course/{courseId}")
    fun getCourseStatus(
        @AuthenticationPrincipal userId: String,
        @PathVariable courseId: UUID
    ): ResponseEntity<List<UnitStatusDTO>> {
        val userUuid = UUID.fromString(userId)
        val statusList = progressService.getCourseProgress(courseId, userUuid)
        return ResponseEntity.ok(statusList)
    }

    @GetMapping("/my-units")
    fun getMyUnits(
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<List<UnitStatusDTO>> {
        val userUuid = UUID.fromString(userId)
        val units = progressService.getAllUnitsForStudent(userUuid)
        return ResponseEntity.ok(units)
    }
}