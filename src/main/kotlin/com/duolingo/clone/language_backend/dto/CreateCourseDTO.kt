package com.duolingo.clone.language_backend.dto

data class CreateCourseDTO(
    val title: String,
    val targetLanguage: String,
    val baseLanguage: String
)
