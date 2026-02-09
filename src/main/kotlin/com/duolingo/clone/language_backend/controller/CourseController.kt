package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.CreateCourseDTO
import com.duolingo.clone.language_backend.entity.CourseEntity
import com.duolingo.clone.language_backend.entity.UnitEntity
import com.duolingo.clone.language_backend.repository.UnitRepository
import com.duolingo.clone.language_backend.repository.CourseRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import com.duolingo.clone.language_backend.service.CourseService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal   // 👈 IMPORTANTE
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/courses")
class CourseController(
    private val courseService: CourseService,
    private val unitRepository: UnitRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository
) {

    // 🔹 NUEVO: cursos del profesor logueado
    @GetMapping("/my")
    fun getMyCourses(@AuthenticationPrincipal userId: String): List<CourseEntity> {
        val uuid = UUID.fromString(userId)
        return courseRepository.findByTeacherId(uuid)
    }

    // GET /api/courses
    @GetMapping
    fun getAllCourses(): List<CourseEntity> {
        return courseService.findActiveCourses()
    }

    // GET /api/courses/{id}
    @GetMapping("/{id}")
    fun getCourseById(@PathVariable id: UUID): ResponseEntity<CourseEntity> {
        val course = courseService.findCourseById(id)
        return if (course != null) ResponseEntity.ok(course) else ResponseEntity.notFound().build()
    }

    // GET /api/courses/{courseId}/units
    @GetMapping("/{courseId}/units")
    fun getUnitsByCourse(@PathVariable courseId: UUID): ResponseEntity<List<UnitEntity>> {
        val units = unitRepository.findAllByCourseIdOrderByUnitOrderAsc(courseId)
        return ResponseEntity.ok(units)
    }

    @PostMapping
    fun createCourse(@RequestBody dto: CreateCourseDTO): ResponseEntity<CourseEntity> {

        val course = CourseEntity(
            title = dto.title,
            baseLanguage = dto.baseLanguage,
            targetLanguage = dto.targetLanguage
        )

        // 🔹 Asignar profesor principal si hay uno
        if (dto.teachers.isNotEmpty()) {
            val teacher = userRepository.findById(dto.teachers.first()).orElseThrow()
            course.teacher = teacher
        }

        // 🔹 Asignar estudiantes
        val students = dto.students.map { userRepository.findById(it).orElseThrow() }
        course.students.addAll(students)

        return ResponseEntity.ok(courseRepository.save(course))
    }
/*
    @PutMapping("/{id}")
    fun updateCourse(
        @PathVariable id: UUID,
        @RequestBody updatedCourse: CourseEntity
    ): ResponseEntity<CourseEntity> {
        return try {
            val course = courseService.updateCourse(id, updatedCourse)
            ResponseEntity.ok(course)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }*/

    @DeleteMapping("/{id}")
    fun deleteCourse(@PathVariable id: UUID): ResponseEntity<Void> {
        courseService.deleteCourse(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{courseId}/assign-teacher/{teacherId}")
    fun assignTeacherToCourse(
        @PathVariable courseId: UUID,
        @PathVariable teacherId: UUID
    ): ResponseEntity<String> {

        val course = courseRepository.findById(courseId)
            .orElseThrow { RuntimeException("Curso no encontrado") }

        val teacher = userRepository.findById(teacherId)
            .orElseThrow { RuntimeException("Profesor no encontrado") }

        course.teacher = teacher
        courseRepository.save(course)

        return ResponseEntity.ok("Profesor asignado al curso")
    }

    @PostMapping("/{courseId}/assign-student/{studentId}")
    fun assignStudentToCourse(
        @PathVariable courseId: UUID,
        @PathVariable studentId: UUID
    ): ResponseEntity<String> {

        val course = courseRepository.findById(courseId)
            .orElseThrow { RuntimeException("Curso no encontrado") }

        val student = userRepository.findById(studentId)
            .orElseThrow { RuntimeException("Estudiante no encontrado") }

        course.students.add(student)
        courseRepository.save(course)

        return ResponseEntity.ok("Estudiante asignado al curso")
    }

    @PutMapping("/{id}")
    fun updateCourse(
        @PathVariable id: UUID,
        @RequestBody dto: CreateCourseDTO
    ): ResponseEntity<CourseEntity> {

        val course = courseRepository.findById(id)
            .orElseThrow { RuntimeException("Curso no encontrado con ID: $id") }

        // Campos básicos
        course.title = dto.title
        course.baseLanguage = dto.baseLanguage
        course.targetLanguage = dto.targetLanguage

        // PROFESOR PRINCIPAL
        if (dto.teachers.isNotEmpty()) {
            val teacher = userRepository.findById(dto.teachers.first())
                .orElseThrow { RuntimeException("Profesor no encontrado") }
            course.teacher = teacher
        } else {
            course.teacher = null // o deja el existente si prefieres
        }

        // ESTUDIANTES
        course.students.clear()
        val students = dto.students.map { userId ->
            userRepository.findById(userId)
                .orElseThrow { RuntimeException("Estudiante no encontrado: $userId") }
        }
        course.students.addAll(students)

        val saved = courseRepository.save(course)
        return ResponseEntity.ok(saved)
    }

}
