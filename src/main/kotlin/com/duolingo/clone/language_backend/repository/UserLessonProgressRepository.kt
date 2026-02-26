package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.LessonEntity
import com.duolingo.clone.language_backend.entity.UserLessonProgressEntity
import com.duolingo.clone.language_backend.entity.UserLessonProgressId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface UserLessonProgressRepository :
    JpaRepository<UserLessonProgressEntity, UserLessonProgressId> {

    fun findByUserId(userId: UUID): List<UserLessonProgressEntity>

    fun findByUserIdAndLessonId(
        userId: UUID,
        lessonId: UUID
    ): UserLessonProgressEntity?

    fun findAllByUserIdAndLessonIdIn(
        userId: UUID,
        lessonIds: List<UUID>
    ): List<UserLessonProgressEntity>

    @Query(
        """
        SELECT COUNT(ulp)
        FROM UserLessonProgressEntity ulp
        WHERE ulp.user.id = :userId
    """
    )
    fun countByUserId(@Param("userId") userId: UUID): Long

    @Query(
        """
        SELECT COUNT(ulp)
        FROM UserLessonProgressEntity ulp
        WHERE ulp.user.id = :userId
          AND ulp.isCompleted = true
          AND ulp.mistakesCount = 0
    """
    )
    fun countPerfectLessons(@Param("userId") userId: UUID): Long

    @Query(
        """
        SELECT l
        FROM LessonEntity l
        LEFT JOIN UserLessonProgressEntity ulp
          ON l.id = ulp.lesson.id
         AND ulp.user.id = :userId
       WHERE ulp.lesson.id IS NULL OR ulp.isCompleted = false
        ORDER BY l.lessonOrder ASC
    """
    )
    fun findNextUncompletedLesson(@Param("userId") userId: UUID): LessonEntity?

    // 🔥 Para las stats del día: usamos lastPracticed
    @Query(
        """
        SELECT ulp
        FROM UserLessonProgressEntity ulp
        WHERE ulp.user.id = :userId
          AND ulp.lastPracticed BETWEEN :start AND :end
    """
    )
    fun findByUserIdAndCompletedAtBetween(
        @Param("userId") userId: UUID,
        @Param("start") start: Instant,
        @Param("end") end: Instant
    ): List<UserLessonProgressEntity>
}