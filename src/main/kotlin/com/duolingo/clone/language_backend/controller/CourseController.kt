package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.entity.CourseEntity
import com.duolingo.clone.language_backend.entity.UnitEntity
import com.duolingo.clone.language_backend.repository.UnitRepository
import com.duolingo.clone.language_backend.service.CourseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/courses")
class CourseController(
    private val courseService: CourseService,
    private val unitRepository: UnitRepository
) {

    // GET /api/courses (Ruta pública/autenticada para ver la lista de cursos)
    @GetMapping
    fun getAllCourses(): List<CourseEntity> {
        // En un entorno real, solo retornarías cursos activos, no todos los CourseEntity
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
    // POST /api/courses (Ruta PROTEGIDA: Solo TEACHER/ADMIN pueden crear)
    // Spring Security validará que el usuario autenticado tenga el rol adecuado.
    @PostMapping
    fun createCourse(@RequestBody course: CourseEntity): ResponseEntity<CourseEntity> {
        try {
            val newCourse = courseService.createCourse(course)
            return ResponseEntity.status(HttpStatus.CREATED).body(newCourse)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
    }

    // PUT /api/courses/{id} (Ruta PROTEGIDA: Solo TEACHER/ADMIN pueden actualizar)
    @PutMapping("/{id}")
    fun updateCourse(@PathVariable id: UUID, @RequestBody updatedCourse: CourseEntity): ResponseEntity<CourseEntity> {
        try {
            val course = courseService.updateCourse(id, updatedCourse)
            return ResponseEntity.ok(course)
        } catch (e: NoSuchElementException) {
            return ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteCourse(@PathVariable id: UUID): ResponseEntity<Void> {
        courseService.deleteCourse(id)
        return ResponseEntity.noContent().build()
    }


}