package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.LessonRequest
import com.duolingo.clone.language_backend.dto.QuestionRequest
import com.duolingo.clone.language_backend.dto.UnitRequest
import com.duolingo.clone.language_backend.entity.*
import com.duolingo.clone.language_backend.service.ContentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID // <-- ¡Asegúrate de importar UUID!

@RestController
@RequestMapping("/api/teacher/content")
class ContentController(
    private val contentService: ContentService
) {
    // Nota: Spring Security ya protege esta ruta con hasAnyAuthority("TEACHER", "ADMIN")

    @GetMapping("/init")
    fun initializeQuestionTypes(): ResponseEntity<String> {
        contentService.ensureQuestionTypesExist()
        return ResponseEntity.ok("Tipos de pregunta asegurados.")
    }

    // --- UNIDADES ---
    @PostMapping("/units")
    fun createUnit(@RequestBody request: UnitRequest): ResponseEntity<UnitEntity> {
        // ... (tu código existente)
        try {
            val unit = contentService.createUnit(request)
            return ResponseEntity.status(HttpStatus.CREATED).body(unit)
        } catch (e: NoSuchElementException) {
            return ResponseEntity.badRequest().build()
        }
    }

    // --- LECCIONES ---
    @PostMapping("/lessons")
    fun createLesson(@RequestBody request: LessonRequest): ResponseEntity<LessonEntity> {
        // ... (tu código existente)
        try {
            val lesson = contentService.createLesson(request)
            return ResponseEntity.status(HttpStatus.CREATED).body(lesson)
        } catch (e: NoSuchElementException) {
            return ResponseEntity.badRequest().build()
        }
    }

    // --- PREGUNTAS ---

    // POST /api/teacher/content/questions
    @PostMapping("/questions")
    fun createQuestion(@RequestBody request: QuestionRequest): ResponseEntity<QuestionEntity> {
        try {
            val question = contentService.createQuestion(request)
            return ResponseEntity.status(HttpStatus.CREATED).body(question)
        } catch (e: NoSuchElementException) {
            return ResponseEntity.badRequest().build()
        }
    }

    // --- ¡NUEVOS ENDPOINTS AÑADIDOS! ---

    // GET /api/teacher/content/lessons/{lessonId}/questions
    @GetMapping("/lessons/{lessonId}/questions")
    fun getQuestionsForLesson(@PathVariable lessonId: UUID): ResponseEntity<List<QuestionEntity>> {
        return try {
            val questions = contentService.getQuestionsByLesson(lessonId)
            ResponseEntity.ok(questions)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    // DELETE /api/teacher/content/questions/{questionId}
    @DeleteMapping("/questions/{questionId}")
    fun deleteQuestion(@PathVariable questionId: UUID): ResponseEntity<Void> {
        return try {
            contentService.deleteQuestion(questionId)
            ResponseEntity.noContent().build() // 204 No Content
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
}