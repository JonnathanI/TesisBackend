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
import java.math.BigDecimal // IMPORTANTE: Para corregir los errores de tipos de punto flotante

@Service
class ProgressService(
    private val lessonRepository: LessonRepository,
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val userLessonProgressRepository: UserLessonProgressRepository,
    private val transactionLogRepository: TransactionLogRepository,
    private val userQuestionDataRepository: UserQuestionDataRepository,
    private val unitRepository: UnitRepository
) {
    private val XP_PER_CORRECT_ANSWER = 1L
    private val LINGOTS_PER_LESSON = 10

    private val MAX_HEARTS = 3
    private val REFILL_TIME_MINUTES: Long = 240

    // CONSTANTE AÑADIDA para la sesión de práctica
    private val PRACTICE_SIZE = 15


    /**
     * Lógica para calcular y actualizar la racha del usuario,
     * incluyendo la protección del Congelador de Racha.
     */
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
                // Lógica de Ruptura de Racha
                if (user.hasStreakFreeze) {
                    // ¡PROTECCIÓN ACTIVA! Consume el congelador y mantiene la racha.
                    user.hasStreakFreeze = false // Lo establece a false para consumirlo
                    // user.currentStreak se mantiene igual (racha congelada)
                } else {
                    // Sin protección, la racha se rompe
                    user.currentStreak = 1
                }
            }
        }
        user.lastPracticeDate = Instant.now()
    }

    /**
     * Comprueba y rellena los corazones del usuario según el tiempo transcurrido.
     */
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
                (heartsGained * REFILL_TIME_MINUTES),
                ChronoUnit.MINUTES
            )
        }
    }


    fun getQuestionsForLesson(lessonId: UUID): List<QuestionEntity> {
        val allQuestions = questionRepository.findAllByLessonId(lessonId)
        return allQuestions.shuffled().take(5)
    }

    fun submitAnswer(userId: UUID, submission: AnswerSubmission): AnswerSubmission {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("Usuario no encontrado.") }

        checkAndRefillHearts(user)

        val question = questionRepository.findById(submission.questionId)
            .orElseThrow { NoSuchElementException("Pregunta no encontrada.") }

        // 1. Limpieza de nulos y espacios (Evita el error que tenías)
        val cleanUserAnswer = submission.userAnswer?.trim() ?: ""
        val cleanTargetAnswer = question.textTarget?.trim() ?: ""

        // 2. Limpieza de signos de puntuación (Ideal para SPEAKING y errores de dedo)
        val finalTarget = cleanTargetAnswer.replace(Regex("[.?,!]"), "")
        val finalUser = cleanUserAnswer.replace(Regex("[.?,!]"), "")

        // 3. Comparación lógica
        val isCorrect = finalTarget.equals(finalUser, ignoreCase = true)

        // 4. Lógica de corazones: SOLO si es incorrecto
        if (!isCorrect) {
            user.heartsCount -= 1
            user.lastHeartRefillTime = Instant.now()

            if (user.heartsCount <= 0) {
                user.heartsCount = 0
                userRepository.save(user)
                throw GameEndException("¡Te has quedado sin corazones! La lección ha terminado.")
            }
        }

        // 5. Guardar cambios y retornar
        userRepository.save(user)
        return submission.copy(isCorrect = isCorrect)
    }

    fun completeLesson(userId: UUID, lessonId: UUID, correctAnswersCount: Int): UserLessonProgressEntity {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("Usuario no encontrado.") }

        checkAndRefillHearts(user)

        val lesson = lessonRepository.findById(lessonId)
            .orElseThrow { NoSuchElementException("Lección no encontrada.") }

        // 1. Calcular XP y Racha
        val xpEarned = correctAnswersCount * XP_PER_CORRECT_ANSWER
        user.xpTotal += xpEarned
        calculateAndSetStreak(user)

        // 2. Ganancia de Lingots y Registro de Transacción
        val lingotsEarned = LINGOTS_PER_LESSON
        user.lingotsCount += lingotsEarned

        userRepository.save(user) // Guarda todos los cambios (XP, Racha, Lingots, Congelador)

        // 3. Progreso de la lección
        val progress = userLessonProgressRepository.findByUserIdAndLessonId(userId, lessonId)
            ?: UserLessonProgressEntity(user = user, lesson = lesson)

        progress.isCompleted = true
        progress.lastPracticed = Instant.now()
        progress.masteryLevel = (progress.masteryLevel ?: 0) + 1 // Incrementa maestría al completar

        return userLessonProgressRepository.save(progress)
    }

    fun getUnitProgress(unitId: UUID, userId: UUID): List<LessonProgressDTO> {
        // 1. Obtener todas las lecciones de la unidad, ya ordenadas.
        val lessons = lessonRepository.findAllByUnitIdOrderByLessonOrderAsc(unitId)

        // 2. Obtener el progreso del usuario para *todas* estas lecciones eficientemente.
        val progressList = userLessonProgressRepository.findAllByUserIdAndLessonIdIn(
            userId,
            lessons.map { it.id!! }
        ).associateBy { it.lesson.id }

        // 3. Mapear y combinar la Lección con su Progreso
        return lessons.map { lesson ->
            val progress = progressList[lesson.id]

            LessonProgressDTO(
                id = lesson.id!!,
                title = lesson.title,
                lessonOrder = lesson.lessonOrder,
                requiredXp = lesson.requiredXp,

                // Mapeo condicional del progreso
                isCompleted = progress?.isCompleted ?: false,
                masteryLevel = progress?.masteryLevel ?: 0,
                lastPracticed = progress?.lastPracticed
            )
        }
    }

    // =================================================================================
    // LÓGICA DE REPASO ESPACIADO: Procesar respuesta
    // =================================================================================

    @Transactional // Aseguramos que todas las operaciones de guardado se completen
    fun submitPracticeAnswer(userId: UUID, submission: PracticeAnswerSubmission) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("Usuario no encontrado.") }

        val question = questionRepository.findById(submission.questionId)
            .orElseThrow { NoSuchElementException("Pregunta no encontrada.") }

        // --- Definición de Constantes (Asumo que están en tu clase o companion object) ---
        // private val XP_PER_CORRECT_ANSWER = 1L
        // private val BASE_SCORE = BigDecimal("0.50")
        // private val MAX_SCORE = BigDecimal("1.0")
        // private val MIN_SCORE = BigDecimal("0.10")

        // 1. Obtener/Crear los datos de la pregunta para el usuario
        val userQuestionData = userQuestionDataRepository
            .findByUserIdAndQuestionId(userId, submission.questionId)
            ?: UserQuestionDataEntity(
                user = user,
                question = question,
                strengthScore = BigDecimal("0.50"),
                masteryLevel = 0, // CRÍTICO: Inicializar correctamente
                lastPracticed = Instant.now(),
                nextDueDate = LocalDateTime.now().plus(1, ChronoUnit.MINUTES) // Listo para practicar
            )

        // 2. Aplicar el Algoritmo de Repaso Espaciado (Simplificado)
        val score = userQuestionData.strengthScore
        val factor = if (submission.isCorrect) BigDecimal("1.0") else BigDecimal("-1.0")

        // Ajuste del score: Aumentar o disminuir la fuerza, limitado entre 0.10 y 1.0
        val adjustment = factor.multiply(BigDecimal("0.15"))
        val newScore = score.add(adjustment).coerceIn(BigDecimal("0.10"), BigDecimal("1.0"))
        userQuestionData.strengthScore = newScore

        // 3. Calcular el Nuevo Intervalo de Repaso
        // CRÍTICO: Es mejor trabajar con una copia modificable de la entidad si quieres usar var en Kotlin data class
        var currentMasteryLevel = userQuestionData.masteryLevel

        val baseDays = when {
            newScore >= BigDecimal("0.90") -> 7L
            newScore >= BigDecimal("0.70") -> 3L
            newScore >= BigDecimal("0.50") -> 1L
            else -> 0L
        }

        // Calcular el nivel de maestría ANTES de usarlo en el cálculo del día
        // Esto es necesario para que el nivel de maestría se ajuste correctamente si falla.
        if (!submission.isCorrect) {
            // CORRECCIÓN LÓGICA: Si es incorrecto, el nivel de maestría debe caer (resetearse o reducirse).
            // Duolingo/SM-2 reducen la fuerza, pero mantenemos el nivel simple. Resetear a 0 es común.
            currentMasteryLevel = 0
        } else {
            // CORRECCIÓN LÓGICA: Si es correcto, el nivel sube, lo cual afecta el multiplicador.
            currentMasteryLevel += 1
        }

        // Aplicar el nuevo nivel de maestría a la entidad
        userQuestionData.masteryLevel = currentMasteryLevel

        // El intervalo crece con el nuevo nivel de maestría
        val daysToNextReview = baseDays * (currentMasteryLevel + 1) // Usamos el nivel ya ajustado

        // CRÍTICO: Cuidado con nextDueDate. Si es incorrecto, debe estar disponible inmediatamente.
        userQuestionData.nextDueDate = if (!submission.isCorrect) {
            // Repetir inmediatamente (próxima sesión)
            LocalDateTime.now().plus(1, ChronoUnit.MINUTES)
        } else {
            LocalDateTime.now().plusDays(daysToNextReview)
        }

        // 4. Actualizar XP y Racha (Solo si es correcta)
        if (submission.isCorrect) {
            // CRÍTICO: La variable XP_PER_CORRECT_ANSWER debe ser definida y ser del mismo tipo que xpTotal.
            user.xpTotal = user.xpTotal + XP_PER_CORRECT_ANSWER // Asumo que XP_PER_CORRECT_ANSWER es un Long
            // Asegúrate de que calculateAndSetStreak exista y maneje la racha.
            calculateAndSetStreak(user)
        }

        // 5. Guardar (Dentro de @Transactional)
        userQuestionData.lastPracticed = Instant.now() // Actualizar la fecha de última práctica
        userQuestionDataRepository.save(userQuestionData)
        userRepository.save(user) // Guarda el XP y la Racha
    }

    // =================================================================================
    // LÓGICA DE PRÁCTICA: Obtener preguntas
    // =================================================================================
    @Transactional // Mantenemos esta anotación, es crucial para Lazy Loading
    fun getPracticeQuestions(userId: UUID): List<QuestionEntity> {
        val now = LocalDateTime.now()
        val practiceList = mutableListOf<QuestionEntity>()

        // PASO 1: Priorizar Preguntas Vencidas (Repaso Espaciado)
        val overdueQuestionsData = userQuestionDataRepository
            .findTop50ByUserIdAndNextDueDateBeforeOrderByNextDueDateAsc(userId, now)

        // Extraer las preguntas y limitar el tamaño (si es necesario)
        practiceList.addAll(overdueQuestionsData.map { it.question }.take(PRACTICE_SIZE))

        // Si ya tenemos suficientes preguntas solo de repaso, las devolvemos
        if (practiceList.size >= PRACTICE_SIZE) {
            return practiceList.shuffled()
        }

        // PASO 2: Complementar con preguntas NUEVAS o de la próxima Lección
        val neededCount = PRACTICE_SIZE - practiceList.size

        // Encontrar la próxima lección NO completada
        val nextLesson = userLessonProgressRepository.findNextUncompletedLesson(userId)
            ?: lessonRepository.findFirstLesson()

        // Obtener preguntas de la lección para rellenar el espacio
        if (nextLesson != null) {
            // Excluir las preguntas de repaso que ya están en la lista
            val excludedIds = practiceList.map { it.id!! }

            // CORRECCIÓN CLAVE:
            // 1. Llamamos al repositorio con SOLO dos argumentos (lessonId, excludedIds).
            // 2. El repositorio debe devolver TODAS las preguntas que cumplen el filtro.
            val allNewQuestions = questionRepository
                .findByLessonIdExcludingIds(nextLesson.id!!, excludedIds) // <-- Solo dos argumentos

            // 3. Aplicamos el límite del resultado en Kotlin para evitar problemas de JPQL/LIMIT.
            val newQuestions = allNewQuestions.take(neededCount)

            practiceList.addAll(newQuestions)
        }

        // Devolver la lista mezclada de repaso y nuevas preguntas
        return practiceList.shuffled()
    }

    fun getCourseProgress(courseId: UUID, userId: UUID): List<UnitStatusDTO> {

        val units = unitRepository.findAllByCourseIdOrderByUnitOrderAsc(courseId)

        val userProgress = userLessonProgressRepository.findByUserId(userId)
        val completedLessonIds = userProgress
            .filter { it.isCompleted }
            .map { it.lesson.id }
            .toSet()

        var isPreviousUnitCompleted = true

        return units.map { unit ->

            val lessons = lessonRepository
                .findAllByUnitIdOrderByLessonOrderAsc(unit.id!!)

            val lessonDTOs = lessons.map { lesson ->
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

            val isUnitCompleted =
                lessons.isNotEmpty() &&
                        lessons.all { completedLessonIds.contains(it.id) }

            val isLocked = !isPreviousUnitCompleted

            if (!isUnitCompleted) {
                isPreviousUnitCompleted = false
            }

            UnitStatusDTO(
                id = unit.id!!,
                title = unit.title,
                unitOrder = unit.unitOrder,
                isLocked = isLocked,
                isCompleted = isUnitCompleted,
                lessons = lessonDTOs
            )
        }
    }

}