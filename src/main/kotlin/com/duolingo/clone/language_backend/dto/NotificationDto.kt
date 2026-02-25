// src/main/kotlin/com/duolingo/clone/language_backend/dto/NotificationDto.kt
package com.duolingo.clone.language_backend.dto

import com.duolingo.clone.language_backend.entity.NotificationEntity

data class NotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val relatedId: String?,
    val createdAt: String,
    val read: Boolean
)

// extensión para mapear Entity -> DTO
fun NotificationEntity.toDto() = NotificationDto(
    id = this.id.toString(),
    type = this.type.name,
    title = this.title,
    message = this.message,
    relatedId = this.relatedId,
    createdAt = this.createdAt.toString(),
    read = this.read
)