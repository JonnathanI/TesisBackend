package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.LessonRequest
import com.duolingo.clone.language_backend.dto.QuestionRequest
import com.duolingo.clone.language_backend.dto.UnitRequest
import com.duolingo.clone.language_backend.entity.*
import com.duolingo.clone.language_backend.repository.*
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.NoSuchElementException

@Service
class ContentService(
    private val courseRepository: CourseRepository,
    private val unitRepository: UnitRepository,
    private val lessonRepository: LessonRepository,
    private val questionRepository: QuestionRepository,
    private val questionTypeRepository: QuestionTypeRepository
) {

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

        // Busca el tipo de pregunta por su ID (UUID)
        val questionType = questionTypeRepository.findById(request.questionTypeId)
            .orElseThrow { NoSuchElementException("Tipo de pregunta no encontrado con ID: ${request.questionTypeId}") }

        val newQuestion = QuestionEntity(
            lesson = lesson,
            questionType = questionType,
            textSource = request.textSource,
            textTarget = request.textTarget,
            options = request.options,
            audioUrl = request.audioUrl,
            hintJson = request.hintJson,
            difficultyScore = request.difficultyScore
        )
        return questionRepository.save(newQuestion)
    }

    // --- ¡NUEVAS FUNCIONES AÑADIDAS! ---

    fun getQuestionsByLesson(lessonId: UUID): List<QuestionEntity> {
        return questionRepository.findAllByLessonId(lessonId)
    }

    fun deleteQuestion(questionId: UUID) {
        if (!questionRepository.existsById(questionId)) {
            throw NoSuchElementException("Pregunta no encontrada con ID: $questionId")
        }
        questionRepository.deleteById(questionId)
    }

    fun ensureQuestionTypesExist() {
        if (questionTypeRepository.count() == 0L) {
            val types = listOf(
                // Assuming QuestionTypeEntity constructor is (typeName: String, description: String)
                // You might need to adjust this if your entity has an ID or other fields first.
                // If it's a data class with a nullable ID as the first param, pass null for it.
                QuestionTypeEntity(typeName = "TRANSLATION_TO_TARGET", description = "Traduce una frase a la lengua objetivo."),
                QuestionTypeEntity(typeName = "SELECT_WORD", description = "Selecciona la palabra correcta para la traducción."),
                QuestionTypeEntity(typeName = "LISTEN_AND_TYPE", description = "Escucha una frase y escríbela."),
                QuestionTypeEntity(typeName = "MATCHING_PAIRS", description = "Empareja palabras con sus traducciones.")
            )
            questionTypeRepository.saveAll(types)
        }
    }
}