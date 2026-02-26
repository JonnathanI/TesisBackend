package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UnitRepository : JpaRepository<UnitEntity, UUID> {

    fun findByCourseIdAndUnitOrder(courseId: UUID, unitOrder: Int): UnitEntity?

    fun findAllByCourseIdOrderByUnitOrderAsc(courseId: UUID): List<UnitEntity>

    fun findAllByCourseTeacherIdOrderByUnitOrderAsc(teacherId: UUID): List<UnitEntity>

    // ⭐ NUEVO: unidades de TODOS los cursos en que está el estudiante
    @Query(
        """
        SELECT u
        FROM UnitEntity u
        JOIN u.course c
        JOIN c.students s
        WHERE s.id = :studentId
        ORDER BY u.unitOrder ASC
        """
    )
    fun findAllByStudentIdOrderByUnitOrderAsc(
        @Param("studentId") studentId: UUID
    ): List<UnitEntity>
}