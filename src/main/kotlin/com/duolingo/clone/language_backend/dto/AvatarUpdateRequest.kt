package com.duolingo.clone.language_backend.dto

data class AvatarUpdateRequest(
    val avatarData: String // Recibiremos el JSON stringificado desde React
)