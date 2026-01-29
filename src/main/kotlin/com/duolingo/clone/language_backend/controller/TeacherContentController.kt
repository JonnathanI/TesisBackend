package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.CreateCourseDTO
import com.duolingo.clone.language_backend.dto.QuestionRequest
import com.duolingo.clone.language_backend.entity.CourseEntity
import com.duolingo.clone.language_backend.entity.LessonEntity
import com.duolingo.clone.language_backend.entity.QuestionEntity
import com.duolingo.clone.language_backend.entity.UnitEntity
import com.duolingo.clone.language_backend.repository.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

/**
 * Controlador para la gestión de contenido académico (Unidades, Lecciones y Preguntas).
 * Incluye correcciones para el manejo de tipos numéricos desde JSON y soporte para gamificación.
 */
@RestController
@RequestMapping("/api/teacher/content")
class TeacherContentController(
    private val unitRepository: UnitRepository,
    private val lessonRepository: LessonRepository,
    private val questionRepository: QuestionRepository,
    private val courseRepository: CourseRepository,
    private val questionTypeRepository: QuestionTypeRepository
) {

    // ==========================================
    // 1. GESTIÓN DE UNIDADES
    // ==========================================
    @GetMapping("/units")
    fun getAllUnits(): ResponseEntity<List<UnitEntity>> {
        return ResponseEntity.ok(unitRepository.findAll())
    }



    @PostMapping("/units")
    fun createUnit(@RequestBody request: Map<String, Any>): ResponseEntity<UnitEntity> {
        val courseId = UUID.fromString(request["courseId"] as String)
        val title = request["title"] as String
        // Corrección: Jackson puede deserializar números como Long o Double.
        // Usar 'as Number' permite convertir de forma segura a Int.
        val order = (request["unitOrder"] as Number).toInt()

        val course = courseRepository.findById(courseId)
            .orElseThrow { RuntimeException("Curso no encontrado con ID: $courseId") }

        val unit = UnitEntity(title = title, unitOrder = order, course = course)
        return ResponseEntity.ok(unitRepository.save(unit))
    }

    @PutMapping("/units/{id}")
    fun updateUnit(@PathVariable id: UUID, @RequestBody request: Map<String, Any>): ResponseEntity<UnitEntity> {
        val unit = unitRepository.findById(id)
            .orElseThrow { RuntimeException("Unidad no encontrada") }

        val newTitle = request["title"] as String
        val newOrder = (request["unitOrder"] as Number).toInt()

        val updatedUnit = unit.copy(title = newTitle, unitOrder = newOrder)
        return ResponseEntity.ok(unitRepository.save(updatedUnit))
    }

    @DeleteMapping("/units/{id}")
    fun deleteUnit(@PathVariable id: UUID): ResponseEntity<Void> {
        unitRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }

    // ==========================================
    // 2. GESTIÓN DE LECCIONES
    // ==========================================

    @PostMapping("/lessons")
    fun createLesson(@RequestBody request: Map<String, Any>): ResponseEntity<LessonEntity> {
        val unitId = UUID.fromString(request["unitId"] as String)
        val title = request["title"] as String
        val order = (request["lessonOrder"] as Number).toInt()
        val xp = (request["requiredXp"] as Number).toInt()

        val unit = unitRepository.findById(unitId)
            .orElseThrow { RuntimeException("Unidad no encontrada") }

        val lesson = LessonEntity(title = title, lessonOrder = order, requiredXp = xp, unit = unit)
        return ResponseEntity.ok(lessonRepository.save(lesson))
    }

    @PutMapping("/lessons/{id}")
    fun updateLesson(@PathVariable id: UUID, @RequestBody request: Map<String, Any>): ResponseEntity<LessonEntity> {
        val lesson = lessonRepository.findById(id)
            .orElseThrow { RuntimeException("Lección no encontrada") }

        val newTitle = request["title"] as String
        val newOrder = (request["lessonOrder"] as Number).toInt()

        val updatedLesson = lesson.copy(title = newTitle, lessonOrder = newOrder)
        return ResponseEntity.ok(lessonRepository.save(updatedLesson))
    }

    @DeleteMapping("/lessons/{id}")
    fun deleteLesson(@PathVariable id: UUID): ResponseEntity<Void> {
        lessonRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }

    // ==========================================
    // 3. GESTIÓN DE PREGUNTAS (GAMIFICADAS)
    // ==========================================

    @GetMapping("/lessons/{lessonId}/questions")
    fun getQuestionsByLesson(@PathVariable lessonId: UUID): ResponseEntity<List<QuestionEntity>> {
        // En una implementación real se recomienda un método en el repositorio 'findByLessonId'
        val questions = questionRepository.findAll().filter { it.lesson?.id == lessonId }
        return ResponseEntity.ok(questions)
    }

    @PostMapping("/questions")
    fun createQuestion(@RequestBody dto: QuestionRequest): ResponseEntity<QuestionEntity> {

        val lessonId = dto.lessonId ?: throw RuntimeException("El lessonId es requerido para crear preguntas de lección")

        val lesson = lessonRepository.findById(lessonId)
            .orElseThrow { RuntimeException("Lección no encontrada") }

        val type = questionTypeRepository.findById(dto.questionTypeId)
            .orElseThrow {
                RuntimeException("Tipo de pregunta no encontrado con ID: ${dto.questionTypeId}")
            }

        val question = QuestionEntity(
            textSource = dto.textSource,
            textTarget = dto.textTarget,
            options = dto.options,
            lesson = lesson,
            questionType = type,
            category = type.typeName, // ✅ coherente con BD
            audioUrl = dto.audioUrl,
            difficultyScore = dto.difficultyScore
        )

        return ResponseEntity.ok(questionRepository.save(question))
    }





    @DeleteMapping("/questions/{id}")
    fun deleteQuestion(@PathVariable id: UUID): ResponseEntity<Void> {
        questionRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/courses")
    fun createCourse(
        @RequestBody dto: CreateCourseDTO
    ): ResponseEntity<CourseEntity> {

        val course = CourseEntity(
            title = dto.title,
            targetLanguage = dto.targetLanguage,
            baseLanguage = dto.baseLanguage
        )

        return ResponseEntity.ok(courseRepository.save(course))
    }

    @PutMapping("/questions/{id}")
    fun updateQuestion(
        @PathVariable id: UUID,
        @RequestBody dto: QuestionRequest
    ): ResponseEntity<QuestionEntity> {
        val question = questionRepository.findById(id)
            .orElseThrow { RuntimeException("Pregunta no encontrada") }

        val type = questionTypeRepository.findById(dto.questionTypeId)
            .orElseThrow { RuntimeException("Tipo de pregunta no encontrado") }

        // Actualización de campos
        question.textSource = dto.textSource
        question.textTarget = dto.textTarget
        question.options = dto.options
        question.questionType = type
        question.category = type.typeName
        question.audioUrl = dto.audioUrl
        question.active = dto.active // Aquí es donde el ojo de la UI hace efecto

        return ResponseEntity.ok(questionRepository.save(question))
    }
}