package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.*
import com.duolingo.clone.language_backend.entity.CourseEntity
import com.duolingo.clone.language_backend.entity.LessonEntity
import com.duolingo.clone.language_backend.entity.QuestionEntity
import com.duolingo.clone.language_backend.entity.UnitEntity
import com.duolingo.clone.language_backend.repository.*
import com.duolingo.clone.language_backend.service.CloudinaryService
import com.duolingo.clone.language_backend.service.CurrentUserService
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
    private val evaluationRepository: EvaluationRepository,
    private val currentUserService: CurrentUserService,   // 👈 NUEVO
    private val userRepository: UserRepository            // 👈 NUEVO
) {

    // ==========================================
    // 0. CURSOS DEL PROFESOR
    // ==========================================
    // 🔹 LISTAR SOLO LOS CURSOS DEL PROFESOR LOGUEADO
    @GetMapping("/courses")
    fun getMyCourses(): ResponseEntity<List<CourseEntity>> {
        val teacherId = currentUserService.getCurrentUserId()
        val courses = courseRepository.findByTeacherId(teacherId)
        return ResponseEntity.ok(courses)
    }

    // 🔹 CREAR CURSO COMO PROFESOR (se asigna automáticamente al profe actual)
    @PostMapping("/courses")
    fun createCourseAsTeacher(@RequestBody dto: CreateCourseDTO): ResponseEntity<CourseEntity> {
        val teacherId = currentUserService.getCurrentUserId()
        val teacher = userRepository.findById(teacherId)
            .orElseThrow { RuntimeException("Profesor no encontrado") }

        val course = CourseEntity(
            title = dto.title,
            baseLanguage = dto.baseLanguage,
            targetLanguage = dto.targetLanguage,
            teacher = teacher
        )

        val saved = courseRepository.save(course)
        return ResponseEntity.ok(saved)
    }

    // ==========================================
    // 1. GESTIÓN DE UNIDADES
    // ==========================================

    // 🔹 SOLO UNIDADES DE CURSOS DEL PROFESOR
    @GetMapping("/units")
    fun getAllUnits(): ResponseEntity<List<UnitDTO>> {
        val teacherId = currentUserService.getCurrentUserId()
        val units = unitRepository.findAllByCourseTeacherIdOrderByUnitOrderAsc(teacherId)

        val dtoList = units.map { u ->
            UnitDTO(
                id = u.id!!,
                title = u.title,
                unitOrder = u.unitOrder,
                courseId = u.course.id!!     // 👈 AQUÍ VIENE EL courseId
            )
        }

        println("🔎 [TeacherContentController] teacherId=$teacherId, unidades=${dtoList.size}")
        return ResponseEntity.ok(dtoList)
    }

    @PostMapping("/units")
    fun createUnit(@RequestBody dto: UnitRequest): ResponseEntity<UnitEntity> {
        val teacherId = currentUserService.getCurrentUserId()

        val course = courseRepository.findById(dto.courseId)
            .orElseThrow { RuntimeException("Curso no encontrado") }

        // 🔒 Validar que el curso pertenece al profe actual
        if (course.teacher?.id != teacherId) {
            throw RuntimeException("No tienes permiso para modificar este curso")
        }

        val unit = UnitEntity(
            title = dto.title,
            unitOrder = dto.unitOrder,
            course = course
        )

        return ResponseEntity.ok(unitRepository.save(unit))
    }

    @PutMapping("/units/{id}")
    fun updateUnit(
        @PathVariable id: UUID,
        @RequestBody dto: UpdateUnitRequest
    ): ResponseEntity<UnitEntity> {

        val teacherId = currentUserService.getCurrentUserId()

        val unit = unitRepository.findById(id)
            .orElseThrow { RuntimeException("Unidad no encontrada") }

        // 🔒 Validar que la unidad pertenece a un curso del profe
        if (unit.course.teacher?.id != teacherId) {
            throw RuntimeException("No tienes permiso para modificar esta unidad")
        }

        val updatedUnit = unit.copy(
            title = dto.title,
            unitOrder = dto.unitOrder
        )

        val saved = unitRepository.save(updatedUnit)
        return ResponseEntity.ok(saved)
    }

    @DeleteMapping("/units/{id}")
    fun deleteUnit(@PathVariable id: UUID): ResponseEntity<Void> {
        val teacherId = currentUserService.getCurrentUserId()

        val unit = unitRepository.findById(id)
            .orElseThrow { RuntimeException("Unidad no encontrada") }

        // 🔒 Validar que la unidad pertenece a un curso del profe
        if (unit.course.teacher?.id != teacherId) {
            throw RuntimeException("No tienes permiso para borrar esta unidad")
        }

        unitRepository.delete(unit)
        return ResponseEntity.ok().build()
    }

    // ==========================================
    // 2. GESTIÓN DE LECCIONES
    // ==========================================

    @PostMapping("/lessons")
    fun createLesson(@RequestBody request: Map<String, Any>): ResponseEntity<LessonEntity> {
        val teacherId = currentUserService.getCurrentUserId()

        val unitId = UUID.fromString(request["unitId"] as String)
        val unit = unitRepository.findById(unitId).orElseThrow { RuntimeException("Unidad no encontrada") }

        // 🔒 Validar que la unidad pertenece a un curso del profe
        if (unit.course.teacher?.id != teacherId) {
            throw RuntimeException("No tienes permiso para añadir lecciones a esta unidad")
        }

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
        val teacherId = currentUserService.getCurrentUserId()

        val lesson = lessonRepository.findById(id)
            .orElseThrow { RuntimeException("Lección no encontrada") }

        // 🔒 Validar que la lección pertenece a un curso del profe
        if (lesson.unit.course.teacher?.id != teacherId) {
            throw RuntimeException("No tienes permiso para borrar esta lección")
        }

        lessonRepository.delete(lesson)
        return ResponseEntity.ok().build()
    }

    // ==========================================
    // 3. GESTIÓN DE PREGUNTAS (CON ARCHIVOS)
    // ==========================================

    @GetMapping("/lessons/{lessonId}/questions")
    fun getQuestionsByLesson(@PathVariable lessonId: UUID): ResponseEntity<List<QuestionEntity>> {
        val questions = questionRepository.findAll().filter { it.lesson?.id == lessonId }
        return ResponseEntity.ok(questions)
    }

    // 🔹 ACTUALIZAR LECCIÓN
    @PutMapping("/lessons/{id}")
    fun updateLesson(
        @PathVariable id: UUID,
        @RequestBody request: Map<String, Any>
    ): ResponseEntity<LessonEntity> {

        val teacherId = currentUserService.getCurrentUserId()

        // 1. Buscar la lección
        val lesson = lessonRepository.findById(id)
            .orElseThrow { RuntimeException("Lección no encontrada") }

        // 2. Verificar que el curso de esa lección pertenezca al profe logueado
        val unit = lesson.unit
            ?: throw RuntimeException("La lección no tiene unidad asociada")

        val course = unit.course
            ?: throw RuntimeException("La unidad no tiene curso asociado")

        if (course.teacher?.id != teacherId) {
            throw RuntimeException("No tienes permiso para modificar esta lección")
        }

        // 3. Leer campos del body
        val newTitle = request["title"] as? String ?: lesson.title
        val newOrder = (request["lessonOrder"] as? Number)?.toInt() ?: lesson.lessonOrder
        val newRequiredXp = (request["requiredXp"] as? Number)?.toInt() ?: lesson.requiredXp

        // 4. Crear copia actualizada
        val updated = lesson.copy(
            title = newTitle,
            lessonOrder = newOrder,
            requiredXp = newRequiredXp
        )

        // 5. Guardar
        val saved = lessonRepository.save(updated)
        return ResponseEntity.ok(saved)
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

    @GetMapping("/students")
    fun getMyStudents(): ResponseEntity<List<StudentSummaryDTO>> {
        val teacherId = currentUserService.getCurrentUserId()

        val students = userRepository.findStudentsByTeacherId(teacherId)
            .map { user ->
                StudentSummaryDTO(
                    id = user.id!!,
                    fullName = user.fullName,
                    email = user.email,
                    xpTotal = user.xpTotal,
                    currentStreak = user.currentStreak
                )
            }

        return ResponseEntity.ok(students)
    }
}
