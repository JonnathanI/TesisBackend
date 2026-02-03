package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.CreateCourseDTO
import com.duolingo.clone.language_backend.dto.QuestionRequest
import com.duolingo.clone.language_backend.entity.CourseEntity
import com.duolingo.clone.language_backend.entity.LessonEntity
import com.duolingo.clone.language_backend.entity.QuestionEntity
import com.duolingo.clone.language_backend.entity.UnitEntity
import com.duolingo.clone.language_backend.repository.*
import com.duolingo.clone.language_backend.service.CloudinaryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/teacher/content")
class TeacherContentController(
    private val unitRepository: UnitRepository,
    private val lessonRepository: LessonRepository,
    private val questionRepository: QuestionRepository,
    private val courseRepository: CourseRepository,
    private val questionTypeRepository: QuestionTypeRepository,
    private val cloudinaryService: CloudinaryService,
    private val evaluationRepository: EvaluationRepository
) {

    // ==========================================
    // 1. GESTIÓN DE UNIDADES
    // ==========================================
    @GetMapping("/units")
    fun getAllUnits(): ResponseEntity<List<UnitEntity>> = ResponseEntity.ok(unitRepository.findAll())

    @PostMapping("/units")
    fun createUnit(@RequestBody request: Map<String, Any>): ResponseEntity<UnitEntity> {
        val courseId = UUID.fromString(request["courseId"] as String)
        val title = request["title"] as String
        val order = (request["unitOrder"] as Number).toInt()

        val course = courseRepository.findById(courseId)
            .orElseThrow { RuntimeException("Curso no encontrado") }

        val unit = UnitEntity(title = title, unitOrder = order, course = course)
        return ResponseEntity.ok(unitRepository.save(unit))
    }

    @PutMapping("/units/{id}")
    fun updateUnit(@PathVariable id: UUID, @RequestBody request: Map<String, Any>): ResponseEntity<UnitEntity> {
        val unit = unitRepository.findById(id).orElseThrow { RuntimeException("Unidad no encontrada") }
        val updatedUnit = unit.copy(
            title = request["title"] as String,
            unitOrder = (request["unitOrder"] as Number).toInt()
        )
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
        val unit = unitRepository.findById(unitId).orElseThrow { RuntimeException("Unidad no encontrada") }

        val lesson = LessonEntity(
            title = request["title"] as String,
            lessonOrder = (request["lessonOrder"] as Number).toInt(),
            requiredXp = (request["requiredXp"] as Number).toInt(),
            unit = unit
        )
        return ResponseEntity.ok(lessonRepository.save(lesson))
    }

    @DeleteMapping("/lessons/{id}")
    fun deleteLesson(@PathVariable id: UUID): ResponseEntity<Void> {
        lessonRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }

    // ==========================================
    // 3. GESTIÓN DE PREGUNTAS (CORREGIDA)
    // ==========================================

    @GetMapping("/lessons/{lessonId}/questions")
    fun getQuestionsByLesson(@PathVariable lessonId: UUID): ResponseEntity<List<QuestionEntity>> {
        val questions = questionRepository.findAll().filter { it.lesson?.id == lessonId }
        return ResponseEntity.ok(questions)
    }

    @PostMapping("/questions", consumes = ["multipart/form-data"])
    fun createQuestion(
        @ModelAttribute dto: QuestionRequest,
        @RequestParam(value = "imageFiles", required = false) imageFiles: List<MultipartFile>?,
        @RequestParam(value = "audioFile", required = false) audioFile: MultipartFile?
    ): ResponseEntity<QuestionEntity> {
        return ResponseEntity.ok(saveOrUpdateQuestion(null, dto, imageFiles, audioFile))
    }

    @PutMapping("/questions/{id}", consumes = ["multipart/form-data"])
    fun updateQuestion(
        @PathVariable id: UUID,
        @ModelAttribute dto: QuestionRequest,
        @RequestParam(value = "imageFiles", required = false) imageFiles: List<MultipartFile>?,
        @RequestParam(value = "audioFile", required = false) audioFile: MultipartFile?
    ): ResponseEntity<QuestionEntity> {
        return ResponseEntity.ok(saveOrUpdateQuestion(id, dto, imageFiles, audioFile))
    }

    /**
     * Lógica compartida para guardar o actualizar preguntas con archivos
     */
    private fun saveOrUpdateQuestion(
        id: UUID?,
        dto: QuestionRequest,
        imageFiles: List<MultipartFile>?,
        audioFile: MultipartFile?
    ): QuestionEntity {

        val lesson = dto.lessonId?.let {
            lessonRepository.findById(it).orElseThrow { RuntimeException("Lección no encontrada") }
        }

        val evaluation = dto.evaluationId?.let {
            evaluationRepository.findById(it).orElseThrow { RuntimeException("Evaluación no encontrada") }
        }

        if (lesson == null && evaluation == null) {
            throw RuntimeException("Debes enviar lessonId O evaluationId")
        }

        val typeId = dto.questionTypeId ?: throw RuntimeException("Tipo de pregunta requerido")

        val type = questionTypeRepository.findById(typeId)
            .orElseThrow { RuntimeException("Tipo de pregunta no encontrado") }

        val finalAudioUrl = if (audioFile != null && !audioFile.isEmpty) {
            cloudinaryService.uploadFile(audioFile, "audios")
        } else dto.audioUrl

        val processedOptions = mutableListOf<String>()

        if (type.typeName == "IMAGE_SELECT") {
            dto.options.forEachIndexed { idx, txt ->
                val file = imageFiles?.getOrNull(idx)
                val url = if (file != null && !file.isEmpty && file.originalFilename != "placeholder.txt") {
                    cloudinaryService.uploadFile(file, "pregunta_imagenes")
                } else null

                processedOptions.add("""{"value":"$txt","imageUrl":"$url"}""")
            }
        } else {
            processedOptions.addAll(dto.options)
        }

        val question = if (id != null) {
            questionRepository.findById(id).orElseThrow().apply {
                this.textSource = dto.textSource
                this.textTarget = dto.textTarget
                this.options = processedOptions
                this.questionType = type
                this.audioUrl = finalAudioUrl
                this.active = dto.active

                if (evaluation != null) {
                    this.evaluation = evaluation
                    this.lesson = null
                } else if (lesson != null) {
                    this.lesson = lesson
                    this.evaluation = null
                }
            }
        } else {
            QuestionEntity(
                textSource = dto.textSource,
                textTarget = dto.textTarget,
                options = processedOptions,
                lesson = lesson,
                evaluation = evaluation,
                questionType = type,
                category = if (evaluation != null) "EVALUATION" else type.typeName,
                audioUrl = finalAudioUrl,
                difficultyScore = dto.difficultyScore.toBigDecimal(),
                active = dto.active
            )
        }

        return questionRepository.save(question)
    }


    @DeleteMapping("/questions/{id}")
    fun deleteQuestion(@PathVariable id: UUID): ResponseEntity<Void> {
        questionRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/courses")
    fun createCourse(@RequestBody dto: CreateCourseDTO): ResponseEntity<CourseEntity> {
        val course = CourseEntity(title = dto.title, targetLanguage = dto.targetLanguage, baseLanguage = dto.baseLanguage)
        return ResponseEntity.ok(courseRepository.save(course))
    }
}