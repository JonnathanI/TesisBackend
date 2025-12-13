package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.ClassroomEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ClassroomRepository : JpaRepository<ClassroomEntity, UUID> {
    fun findAllByTeacherId(teacherId: UUID): List<ClassroomEntity>
    fun existsByCode(code: String): Boolean
    fun findByCode(code: String): ClassroomEntity?
    fun findAllByStudentsId(studentId: UUID): List<ClassroomEntity>
}