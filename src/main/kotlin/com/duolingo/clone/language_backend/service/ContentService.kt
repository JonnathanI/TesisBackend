package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.LessonRequest
import com.duolingo.clone.language_backend.dto.QuestionRequest
import com.duolingo.clone.language_backend.dto.UnitRequest
import com.duolingo.clone.language_backend.entity.*
import com.duolingo.clone.language_backend.repository.*
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ContentService(
    private val courseRepository: CourseRepository,
    private val unitRepository: UnitRepository,
    private val lessonRepository: LessonRepository,
    private val questionRepository: QuestionRepository,
    private val questionTypeRepository: QuestionTypeRepository
) {

    // ... (tus funciones createUnit y createLesson están bien)
    fun createUnit(request: UnitRequest): UnitEntity {
        val course = courseRepository.findById(request.courseId)
            .orElseThrow { NoSuchElementException("Curso no encontrado con ID: ${request.courseId}") }

        val newUnit = UnitEntity(
            course = course,
            title = request.title,
            unitOrder = request.unitOrder
        )
        return unitRepository.save(newUnit)
    }

    fun createLesson(request: LessonRequest): LessonEntity {
        val unit = unitRepository.findById(request.unitId)
            .orElseThrow { NoSuchElementException("Unidad no encontrada con ID: ${request.unitId}") }

        val newLesson = LessonEntity(
            unit = unit,
            title = request.title,
            lessonOrder = request.lessonOrder,
            requiredXp = request.requiredXp
        )
        return lessonRepository.save(newLesson)
    }


    // --- FUNCIÓN CREATEQUESTION (ACTUALIZADA) ---
    fun createQuestion(request: QuestionRequest): QuestionEntity {
        val lesson = lessonRepository.findById(request.lessonId)
            .orElseThrow { NoSuchElementException("Lección no encontrada con ID: ${request.lessonId}") }

        val questionType = questionTypeRepository.findById(request.questionTypeId)
            .orElseThrow { NoSuchElementException("Tipo de pregunta no encontrado con ID: ${request.questionTypeId}") }

        val newQuestion = QuestionEntity(
            lesson = lesson,
            questionType = questionType,
            textSource = request.textSource,
            textTarget = request.textTarget,
            options = request.options, // <-- ¡AÑADIDO!
            audio_url = request.audioUrl, // Tu entidad usa audio_url
            hintJson = request.hintJson,
            difficultyScore = request.difficultyScore
        )
        return questionRepository.save(newQuestion)
    }

    // --- ¡NUEVAS FUNCIONES AÑADIDAS! ---

    fun getQuestionsByLesson(lessonId: UUID): List<QuestionEntity> {
        // Simplemente devuelve todas las preguntas para esa lección
        return questionRepository.findAllByLessonId(lessonId)
    }

    fun deleteQuestion(questionId: UUID) {
        if (!questionRepository.existsById(questionId)) {
            throw NoSuchElementException("Pregunta no encontrada con ID: $questionId")
        }
        questionRepository.deleteById(questionId)
    }

    // ... (tu función ensureQuestionTypesExist está bien)
    fun ensureQuestionTypesExist() {
        if (questionTypeRepository.count() == 0L) {
            val types = listOf(
                QuestionTypeEntity("TRANSLATION_TO_TARGET", "Traduce una frase a la lengua objetivo."),
                QuestionTypeEntity("SELECT_WORD", "Selecciona la palabra correcta para la traducción."),
                QuestionTypeEntity("LISTEN_AND_TYPE", "Escucha una frase y escríbela."),
                QuestionTypeEntity("MATCHING_PAIRS", "Empareja palabras con sus traducciones.")
            )
            questionTypeRepository.saveAll(types)
        }
    }
}