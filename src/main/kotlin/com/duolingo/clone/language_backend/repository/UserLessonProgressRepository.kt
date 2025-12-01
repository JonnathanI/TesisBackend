package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserLessonProgressRepository : JpaRepository<UserLessonProgressEntity, UserLessonProgressId> {
    fun findByUserId(userId: UUID): List<UserLessonProgressEntity>
    fun findByUserIdAndLessonId(userId: UUID, lessonId: UUID): UserLessonProgressEntity?
    fun findAllByUserIdAndLessonIdIn(userId: UUID, lessonIds: List<UUID>): List<UserLessonProgressEntity>
    @Query("""
        SELECT l FROM LessonEntity l 
        LEFT JOIN UserLessonProgressEntity ulp 
        ON l.id = ulp.lesson.id AND ulp.user.id = :userId
        WHERE (ulp.isCompleted IS NULL OR ulp.isCompleted = FALSE)
        ORDER BY l.lessonOrder ASC
        LIMIT 1
    """)
    fun findNextUncompletedLesson(userId: UUID): LessonEntity?
}