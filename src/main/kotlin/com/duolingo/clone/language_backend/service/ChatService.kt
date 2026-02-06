package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.ChatMessageResponse
import com.duolingo.clone.language_backend.dto.SendMessageRequest
import com.duolingo.clone.language_backend.entity.ChatMessageEntity
import com.duolingo.clone.language_backend.repository.ChatMessageRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class ChatService(
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository
) {

    fun getConversation(currentUserId: UUID, friendId: UUID): List<ChatMessageResponse> {
        val messages = chatMessageRepository.findConversation(currentUserId, friendId)

        return messages.map { msg ->
            ChatMessageResponse(
                id = msg.id!!,
                senderId = msg.sender.id!!,
                receiverId = msg.receiver.id!!,
                content = msg.content,
                createdAt = msg.createdAt
            )
        }
    }

    fun sendMessage(currentUserId: UUID, friendId: UUID, req: SendMessageRequest): ChatMessageResponse {
        val sender = userRepository.findById(currentUserId)
            .orElseThrow { RuntimeException("Usuario actual no encontrado") }

        val receiver = userRepository.findById(friendId)
            .orElseThrow { RuntimeException("Amigo no encontrado") }

        val toSave = ChatMessageEntity(
            sender = sender,
            receiver = receiver,
            content = req.content
        )

        val saved = chatMessageRepository.save(toSave)

        return ChatMessageResponse(
            id = saved.id!!,
            senderId = sender.id!!,
            receiverId = receiver.id!!,
            content = saved.content,
            createdAt = saved.createdAt
        )
    }
}
