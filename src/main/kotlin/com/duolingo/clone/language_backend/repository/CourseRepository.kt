package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CourseRepository : JpaRepository<CourseEntity, UUID> {
    fun findByTitle(title: String): CourseEntity?
    // 🔹 TODOS los cursos de un profesor concreto
    fun findByTeacherId(teacherId: UUID): List<CourseEntity>
}