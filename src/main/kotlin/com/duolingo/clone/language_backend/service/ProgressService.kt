package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.AnswerSubmission
import com.duolingo.clone.language_backend.dto.LessonProgressDTO
import com.duolingo.clone.language_backend.dto.PracticeAnswerSubmission
import com.duolingo.clone.language_backend.dto.UnitStatusDTO
import com.duolingo.clone.language_backend.entity.*
import com.duolingo.clone.language_backend.repository.*
import com.duolingo.clone.language_backend.exception.GameEndException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.LocalDateTime
import java.math.BigDecimal

@Service
class ProgressService(
    private val lessonRepository: LessonRepository,
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val userLessonProgressRepository: UserLessonProgressRepository,
    private val transactionLogRepository: TransactionLogRepository,
    private val userQuestionDataRepository: UserQuestionDataRepository,
    private val unitRepository: UnitRepository,
    private val badgeService: BadgeService
) {

    private val XP_PER_CORRECT_ANSWER = 1L
    private val LINGOTS_PER_LESSON = 10

    private val MAX_HEARTS = 3
    private val REFILL_TIME_MINUTES: Long = 240

    private val PRACTICE_SIZE = 15

    // -------------------------------------------------------------
    // 🔥 RACHA
    // -------------------------------------------------------------
    private fun calculateAndSetStreak(user: UserEntity) {
        val today = Instant.now().truncatedTo(ChronoUnit.DAYS)
        val lastPracticeDay = user.lastPracticeDate?.truncatedTo(ChronoUnit.DAYS)

        if (lastPracticeDay == null) {
            user.currentStreak = 1
        } else if (lastPracticeDay != today) {
            val yesterday = today.minus(1, ChronoUnit.DAYS)

            if (lastPracticeDay == yesterday) {
                user.currentStreak += 1
            } else {
                if (user.hasStreakFreeze) {
                    user.hasStreakFreeze = false
                } else {
                    user.currentStreak = 1
                }
            }
        }

        user.lastPracticeDate = Instant.now()
    }

    // -------------------------------------------------------------
    // ❤️ CORAZONES
    // -------------------------------------------------------------
    private fun checkAndRefillHearts(user: UserEntity) {
        if (user.heartsCount >= MAX_HEARTS) {
            user.lastHeartRefillTime = Instant.now()
            return
        }

        val lastRefillTime = user.lastHeartRefillTime ?: run {
            user.lastHeartRefillTime = Instant.now()
            return
        }

        val now = Instant.now()
        val minutesElapsed = ChronoUnit.MINUTES.between(lastRefillTime, now)

        val heartsGained = (minutesElapsed / REFILL_TIME_MINUTES).toInt()

        if (heartsGained > 0) {
            user.heartsCount = minOf(user.heartsCount + heartsGained, MAX_HEARTS)
            user.lastHeartRefillTime = lastRefillTime.plus(
                heartsGained * REFILL_TIME_MINUTES,
                ChronoUnit.MINUTES
            )
        }
    }

    // -------------------------------------------------------------
    // 📘 PREGUNTAS
    // -------------------------------------------------------------
    fun getQuestionsForLesson(lessonId: UUID): List<QuestionEntity> {
        return questionRepository.findAllByLessonId(lessonId)
            .shuffled()
            .take(5)
    }

    fun submitAnswer(userId: UUID, submission: AnswerSubmission): AnswerSubmission {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("Usuario no encontrado.") }

        checkAndRefillHearts(user)

        val question = questionRepository.findById(submission.questionId)
            .orElseThrow { NoSuchElementException("Pregunta no encontrada.") }

        val cleanUserAnswer = submission.userAnswer?.trim() ?: ""
        val cleanTargetAnswer = question.textTarget?.trim() ?: ""

        val compareUser = cleanUserAnswer.replace(Regex("[.?,!]"), "")
        val compareTarget = cleanTargetAnswer.replace(Regex("[.?,!]"), "")

        val isCorrect = compareUser.equals(compareTarget, ignoreCase = true)

        // Si es incorrecto → perder corazón
        if (!isCorrect) {
            user.heartsCount -= 1
            user.lastHeartRefillTime = Instant.now()

            if (user.heartsCount <= 0) {
                user.heartsCount = 0
                userRepository.save(user)
                throw GameEndException("¡Te has quedado sin corazones! La lección ha terminado.")
            }
        }

        userRepository.save(user)
        return submission.copy(isCorrect = isCorrect)
    }

    // -------------------------------------------------------------
    // 🎉 COMPLETAR LECCIÓN (XP, racha, insignias)
    // -------------------------------------------------------------
    @Transactional
    fun completeLesson(
        userId: UUID,
        lessonId: UUID,
        correct: Int,
        mistakes: Int
    ): UserLessonProgressEntity {

        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("Usuario no encontrado") }

        val lesson = lessonRepository.findById(lessonId)
            .orElseThrow { NoSuchElementException("Lección no encontrada") }

        val progress = userLessonProgressRepository
            .findByUserIdAndLessonId(userId, lessonId)
            ?: UserLessonProgressEntity(
                user = user,
                lesson = lesson
            )

        progress.isCompleted = true
        progress.correctAnswers = correct
        progress.mistakesCount = mistakes
        progress.lastPracticed = Instant.now()

        // XP y lingotes
        user.xpTotal += (correct * XP_PER_CORRECT_ANSWER)
        user.lingotsCount += LINGOTS_PER_LESSON

        // 🔥 RACHA
        calculateAndSetStreak(user)

        // ---------------------------------------------------------
        // 🏆 INSIGNIAS
        // ---------------------------------------------------------

        // 1️⃣ Primera lección completada
        if (userLessonProgressRepository.countByUserId(userId) == 0L) {
            badgeService.giveBadgeIfNotOwned(userId, "FIRST_LESSON")
        }

        // 2️⃣ Lección perfecta (0 errores)
        if (mistakes == 0) {
            badgeService.giveBadgeIfNotOwned(userId, "PERFECT_LESSON")
        }

        // 3️⃣ Rachas
        when (user.currentStreak) {
            3 -> badgeService.giveBadgeIfNotOwned(userId, "STREAK_3")
            7 -> badgeService.giveBadgeIfNotOwned(userId, "STREAK_7")
        }

        userRepository.save(user)
        return userLessonProgressRepository.saveAndFlush(progress)
    }

    // -------------------------------------------------------------
    // 📘 PROGRESO DE UNIDAD
    // -------------------------------------------------------------
    fun getUnitProgress(unitId: UUID, userId: UUID): List<LessonProgressDTO> {
        val lessons = lessonRepository.findAllByUnitIdOrderByLessonOrderAsc(unitId)

        val progressMap = userLessonProgressRepository
            .findAllByUserIdAndLessonIdIn(userId, lessons.map { it.id!! })
            .associateBy { it.lesson.id }

        return lessons.map { lesson ->
            val progress = progressMap[lesson.id]

            LessonProgressDTO(
                id = lesson.id!!,
                title = lesson.title,
                lessonOrder = lesson.lessonOrder,
                requiredXp = lesson.requiredXp,
                isCompleted = progress?.isCompleted ?: false,
                masteryLevel = progress?.masteryLevel ?: 0,
                lastPracticed = progress?.lastPracticed
            )
        }
    }

    // -------------------------------------------------------------
    // 🧠 REPASO ESPACIADO
    // -------------------------------------------------------------
    @Transactional
    fun submitPracticeAnswer(userId: UUID, submission: PracticeAnswerSubmission) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("Usuario no encontrado.") }

        val question = questionRepository.findById(submission.questionId)
            .orElseThrow { NoSuchElementException("Pregunta no encontrada.") }

        val data = userQuestionDataRepository
            .findByUserIdAndQuestionId(userId, submission.questionId)
            ?: UserQuestionDataEntity(
                user = user,
                question = question,
                strengthScore = BigDecimal("0.50"),
                masteryLevel = 0,
                lastPracticed = Instant.now(),
                nextDueDate = LocalDateTime.now().plusMinutes(1)
            )

        val factor = if (submission.isCorrect) BigDecimal("1.0") else BigDecimal("-1.0")
        val newScore = data.strengthScore.add(factor.multiply(BigDecimal("0.15")))
            .coerceIn(BigDecimal("0.10"), BigDecimal("1.0"))

        data.strengthScore = newScore

        var mastery = data.masteryLevel
        mastery = if (submission.isCorrect) mastery + 1 else 0
        data.masteryLevel = mastery

        val baseDays = when {
            newScore >= BigDecimal("0.90") -> 7L
            newScore >= BigDecimal("0.70") -> 3L
            newScore >= BigDecimal("0.50") -> 1L
            else -> 0L
        }

        data.nextDueDate =
            if (!submission.isCorrect)
                LocalDateTime.now().plusMinutes(1)
            else
                LocalDateTime.now().plusDays(baseDays * (mastery + 1))

        if (submission.isCorrect) {
            user.xpTotal += XP_PER_CORRECT_ANSWER
            calculateAndSetStreak(user)
        }

        data.lastPracticed = Instant.now()

        userQuestionDataRepository.save(data)
        userRepository.save(user)
    }

    // -------------------------------------------------------------
    // 📝 OBTENER PREGUNTAS DE PRÁCTICA
    // -------------------------------------------------------------
    @Transactional
    fun getPracticeQuestions(userId: UUID): List<QuestionEntity> {
        val now = LocalDateTime.now()
        val result = mutableListOf<QuestionEntity>()

        val overdue = userQuestionDataRepository
            .findTop50ByUserIdAndNextDueDateBeforeOrderByNextDueDateAsc(userId, now)

        result.addAll(overdue.map { it.question }.take(PRACTICE_SIZE))

        if (result.size >= PRACTICE_SIZE) return result.shuffled()

        val needed = PRACTICE_SIZE - result.size

        val nextLesson = userLessonProgressRepository.findNextUncompletedLesson(userId)
            ?: lessonRepository.findFirstLesson()

        if (nextLesson != null) {
            val excludedIds = result.map { it.id!! }
            val newQuestions = questionRepository.findByLessonIdExcludingIds(
                nextLesson.id!!,
                excludedIds
            ).take(needed)

            result.addAll(newQuestions)
        }

        return result.shuffled()
    }

    // -------------------------------------------------------------
    // 🧩 PROGRESO DEL CURSO
    // -------------------------------------------------------------
    fun getCourseProgress(courseId: UUID, userId: UUID): List<UnitStatusDTO> {

        val units = unitRepository.findAllByCourseIdOrderByUnitOrderAsc(courseId)
        val progress = userLessonProgressRepository.findByUserId(userId)

        val completedLessonIds = progress
            .filter { it.isCompleted }
            .map { it.lesson.id }
            .toSet()

        var previousCompleted = true

        return units.map { unit ->

            val lessons = lessonRepository.findAllByUnitIdOrderByLessonOrderAsc(unit.id!!)

            val DTOs = lessons.map { lesson ->
                LessonProgressDTO(
                    id = lesson.id!!,
                    title = lesson.title,
                    lessonOrder = lesson.lessonOrder,
                    requiredXp = lesson.requiredXp,
                    isCompleted = completedLessonIds.contains(lesson.id),
                    masteryLevel = 0,
                    lastPracticed = null
                )
            }

            val isCompletedUnit =
                lessons.isNotEmpty() &&
                        lessons.all { completedLessonIds.contains(it.id) }

            val isLocked = !previousCompleted

            if (!isCompletedUnit) previousCompleted = false

            // 🔥 Insignia al completar una unidad
            if (isCompletedUnit) {
                badgeService.giveBadgeIfNotOwned(userId, "UNIT_MASTER_${unit.unitOrder}")
            }

            UnitStatusDTO(
                id = unit.id!!,
                title = unit.title,
                unitOrder = unit.unitOrder,
                isLocked = isLocked,
                isCompleted = isCompletedUnit,
                lessons = DTOs
            )
        }
    }
}
