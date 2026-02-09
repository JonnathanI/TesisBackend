package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UnitRepository : JpaRepository<UnitEntity, UUID> {
    // Buscar unidades por curso y orden (para la ruta de aprendizaje)
    fun findByCourseIdAndUnitOrder(courseId: UUID, unitOrder: Int): UnitEntity?
    fun findAllByCourseIdOrderByUnitOrderAsc(courseId: UUID): List<UnitEntity>
    fun findAllByCourseTeacherIdOrderByUnitOrderAsc(teacherId: UUID): List<UnitEntity>
}