package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.entity.CourseEntity
import com.duolingo.clone.language_backend.repository.CourseRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CourseService(
    private val courseRepository: CourseRepository
) {

    fun findAllCourses(): List<CourseEntity> =
        courseRepository.findAll()

    // Por ahora "activos" = todos
    fun findActiveCourses(): List<CourseEntity> =
        courseRepository.findAll()

    fun findCourseById(id: UUID): CourseEntity? =
        courseRepository.findById(id).orElse(null)

    fun createCourse(course: CourseEntity): CourseEntity {
        // Ejemplo de validación: no repetir título
        require(courseRepository.findByTitle(course.title) == null) {
            "Ya existe un curso con este título."
        }
        return courseRepository.save(course)
    }

    fun updateCourse(id: UUID, updatedCourse: CourseEntity): CourseEntity {
        val existingCourse = courseRepository.findById(id)
            .orElseThrow { NoSuchElementException("Curso no encontrado con ID: $id") }

        // Como la entidad es mutable, actualizamos los campos directamente
        existingCourse.title = updatedCourse.title
        existingCourse.baseLanguage = updatedCourse.baseLanguage
        existingCourse.targetLanguage = updatedCourse.targetLanguage

        // Si quieres permitir cambiar el profesor desde el panel:
        existingCourse.teacher = updatedCourse.teacher

        // Normalmente NO tocaríamos students aquí; lo haríamos en métodos específicos
        // (addStudentToCourse, removeStudentFromCourse, etc.)

        return courseRepository.save(existingCourse)
    }

    fun deleteCourse(id: UUID) {
        if (!courseRepository.existsById(id)) {
            throw NoSuchElementException("Curso no encontrado con ID: $id")
        }
        courseRepository.deleteById(id)
    }
}
