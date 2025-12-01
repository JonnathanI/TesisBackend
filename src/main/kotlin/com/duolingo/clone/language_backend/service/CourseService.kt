package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.entity.CourseEntity
import com.duolingo.clone.language_backend.repository.CourseRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CourseService(
    private val courseRepository: CourseRepository
) {

    fun findAllCourses(): List<CourseEntity> {
        return courseRepository.findAll()
    }

    fun findCourseById(id: UUID): CourseEntity? {
        return courseRepository.findById(id).orElse(null)
    }

    fun findActiveCourses(): List<CourseEntity> {
        // Asumiendo que has definido un método en CourseRepository: findByIsActive(Boolean)
        // Por ahora, usaremos un filtro en memoria si no está en el repositorio:
        return courseRepository.findAll().filter { it.isActive }
    }

    fun createCourse(course: CourseEntity): CourseEntity {
        // Lógica de validación, por ejemplo, que no exista un curso con el mismo título
        require(courseRepository.findByTitle(course.title) == null) { "Ya existe un curso con este título." }
        return courseRepository.save(course)
    }

    // Método que solo un ADMIN o TEACHER puede usar
    fun updateCourse(id: UUID, updatedCourse: CourseEntity): CourseEntity {
        val existingCourse = courseRepository.findById(id)
            .orElseThrow { NoSuchElementException("Curso no encontrado con ID: $id") }

        val courseToUpdate = existingCourse.copy(
            title = updatedCourse.title,
            targetLanguage = updatedCourse.targetLanguage,
            baseLanguage = updatedCourse.baseLanguage,
            isActive = updatedCourse.isActive
        )
        return courseRepository.save(courseToUpdate)
    }
}