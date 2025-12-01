package com.duolingo.clone.language_backend.entity

import java.io.Serializable
import java.util.UUID

data class UserQuestionDataId(
    val user: UUID? = null,
    val question: UUID? = null
) : Serializable