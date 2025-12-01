package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.*
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query // ¡Asegúrate de importar Query!
import java.util.UUID

// 4. Repositorio de Lecciones
interface LessonRepository : JpaRepository<LessonEntity, UUID> {
    fun findAllByUnitId(unitId: UUID): List<LessonEntity>
    fun findAllByUnitIdOrderByLessonOrderAsc(unitId: UUID): List<LessonEntity>

    // 💡 CORRECCIÓN DEFINITIVA: Usar @Query para encontrar la lección con menor lessonOrder.
    @Query(value = "SELECT l FROM LessonEntity l ORDER BY l.lessonOrder ASC LIMIT 1")
    @Transactional
    fun findFirstLesson(): LessonEntity? // Renombramos a 'findFirstLesson' para claridad en el service

    // fun findFirstOrderByLessonOrderAsc(): LessonEntity? // Desactiva/borra esta línea si aún está
}