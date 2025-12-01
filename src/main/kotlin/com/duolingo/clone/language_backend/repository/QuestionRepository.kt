package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface QuestionRepository : JpaRepository<QuestionEntity, UUID> {
    fun findAllByLessonId(lessonId: UUID): List<QuestionEntity>

    // Forma JPQL de obtener preguntas aleatorias y excluir IDs
    // Dejamos que Kotlin/Hibernate limiten el resultado, o usamos el take() en el servicio.
    @Query("""
        SELECT q FROM QuestionEntity q
        WHERE q.lesson.id = :lessonId
        AND q.id NOT IN :excludedIds
        ORDER BY FUNCTION('RANDOM') 
    """)
    fun findByLessonIdExcludingIds(
        @Param("lessonId") lessonId: UUID,
        @Param("excludedIds") excludedIds: List<UUID>
    ): List<QuestionEntity>

    // NOTA: El nombre de la función ahora es más simple
}