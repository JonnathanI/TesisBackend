package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.entity.LessonEntity
import com.duolingo.clone.language_backend.entity.QuestionEntity
import com.duolingo.clone.language_backend.entity.UnitEntity
import com.duolingo.clone.language_backend.repository.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/teacher/content") // <--- ESTA ES LA RUTA QUE TE DABA ERROR
class TeacherContentController(
    private val unitRepository: UnitRepository,
    private val lessonRepository: LessonRepository,
    private val questionRepository: QuestionRepository,
    private val courseRepository: CourseRepository,
    private val questionTypeRepository: QuestionTypeRepository
) {

    // 1. CREAR UNIDAD
    @PostMapping("/units")
    fun createUnit(@RequestBody request: Map<String, Any>): ResponseEntity<UnitEntity> {
        val courseId = UUID.fromString(request["courseId"] as String)
        val title = request["title"] as String
        val order = (request["unitOrder"] as Int)

        val course = courseRepository.findById(courseId).orElseThrow()
        val unit = UnitEntity(title = title, unitOrder = order, course = course)
        return ResponseEntity.ok(unitRepository.save(unit))
    }

    // 2. ACTUALIZAR UNIDAD
    @PutMapping("/units/{id}")
    fun updateUnit(@PathVariable id: UUID, @RequestBody request: Map<String, Any>): ResponseEntity<UnitEntity> {
        val unit = unitRepository.findById(id).orElseThrow { RuntimeException("Unidad no encontrada") }
        val newTitle = request["title"] as String
        val newOrder = (request["unitOrder"] as Number).toInt()
        val updatedUnit = unit.copy(title = newTitle, unitOrder = newOrder)
        return ResponseEntity.ok(unitRepository.save(updatedUnit))
    }

    // 3. ELIMINAR UNIDAD
    @DeleteMapping("/units/{id}")
    fun deleteUnit(@PathVariable id: UUID): ResponseEntity<Void> {
        unitRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }

    // --- LECCIONES ---
    @PostMapping("/lessons")
    fun createLesson(@RequestBody request: Map<String, Any>): ResponseEntity<LessonEntity> {
        val unitId = UUID.fromString(request["unitId"] as String)
        val title = request["title"] as String
        val order = (request["lessonOrder"] as Int)
        val xp = (request["requiredXp"] as Int)

        val unit = unitRepository.findById(unitId).orElseThrow()
        val lesson = LessonEntity(title = title, lessonOrder = order, requiredXp = xp, unit = unit)
        return ResponseEntity.ok(lessonRepository.save(lesson))
    }

    @PutMapping("/lessons/{id}")
    fun updateLesson(@PathVariable id: UUID, @RequestBody request: Map<String, Any>): ResponseEntity<LessonEntity> {
        val lesson = lessonRepository.findById(id).orElseThrow { RuntimeException("Lección no encontrada") }
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

    // --- PREGUNTAS ---
    @GetMapping("/lessons/{lessonId}/questions")
    fun getQuestionsByLesson(@PathVariable lessonId: UUID): ResponseEntity<List<QuestionEntity>> {
        val questions = questionRepository.findAll().filter { it.lesson.id == lessonId }
        return ResponseEntity.ok(questions)
    }

    @PostMapping("/questions")
    fun createQuestion(@RequestBody request: Map<String, Any>): ResponseEntity<QuestionEntity> {
        val lessonId = UUID.fromString(request["lessonId"] as String)
        val typeId = request["questionTypeId"] as String
        val textSource = request["textSource"] as String
        val textTarget = request["textTarget"] as String
        val options = request["options"] as List<String>

        val lesson = lessonRepository.findById(lessonId).orElseThrow()
        val type = questionTypeRepository.findByTypeName(typeId)
            ?: throw RuntimeException("Tipo de pregunta inválido")

        val question = QuestionEntity(
            textSource = textSource,
            textTarget = textTarget,
            options = options,
            lesson = lesson,
            questionType = type,
            difficultyScore = BigDecimal.ONE
        )
        return ResponseEntity.ok(questionRepository.save(question))
    }

    @PutMapping("/questions/{id}")
    fun updateQuestion(@PathVariable id: UUID, @RequestBody request: Map<String, Any>): ResponseEntity<QuestionEntity> {
        val question = questionRepository.findById(id).orElseThrow { RuntimeException("Pregunta no encontrada") }
        val newTextSource = request["textSource"] as String
        val newTextTarget = request["textTarget"] as String
        val newOptions = request["options"] as List<String>

        val updatedQuestion = question.copy(
            textSource = newTextSource,
            textTarget = newTextTarget,
            options = newOptions
        )
        return ResponseEntity.ok(questionRepository.save(updatedQuestion))
    }

    @DeleteMapping("/questions/{id}")
    fun deleteQuestion(@PathVariable id: UUID): ResponseEntity<Void> {
        questionRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }
}