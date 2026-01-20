package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.QuestionTypeResponse
import com.duolingo.clone.language_backend.repository.QuestionTypeRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/question-types")
class QuestionTypeController(
    private val questionTypeRepository: QuestionTypeRepository
) {

    @GetMapping
    fun getAll(): List<QuestionTypeResponse> {
        return questionTypeRepository.findAll().map {
            QuestionTypeResponse(
                id = it.id!!,
                typeName = it.typeName,
                description = it.description
            )
        }
    }
}
