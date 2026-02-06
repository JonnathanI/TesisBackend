package com.duolingo.clone.language_backend.dto

import java.time.Instant
import java.util.UUID

data class ChatMessageResponse(
    val id: Long,
    val senderId: UUID,
    val receiverId: UUID,
    val content: String,
    val createdAt: Instant,
    val attachmentUrl: String? = null,
    val attachmentType: String? = null
)


data class SendMessageRequest(
    val content: String
)
