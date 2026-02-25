package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.ChatMessageResponse
import com.duolingo.clone.language_backend.dto.SendMessageRequest
import com.duolingo.clone.language_backend.entity.ChatMessageEntity
import com.duolingo.clone.language_backend.entity.NotificationType
import com.duolingo.clone.language_backend.repository.ChatMessageRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID

@Service
class ChatService(
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService,
    private val notificationService: NotificationService          // 👈 AÑADIDO
) {

    fun getConversation(currentUserId: UUID, friendId: UUID): List<ChatMessageResponse> {
        val messages = chatMessageRepository.findConversation(currentUserId, friendId)

        return messages.map { msg ->
            ChatMessageResponse(
                id = msg.id!!,
                senderId = msg.sender.id!!,
                receiverId = msg.receiver.id!!,
                content = msg.content,
                createdAt = msg.createdAt,
                attachmentUrl = msg.attachmentUrl,
                attachmentType = msg.attachmentType
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

        // 🔔 NOTIFICACIÓN PARA EL RECEPTOR
        notificationService.createNotification(
            user = receiver,
            type = NotificationType.MESSAGE_RECEIVED,
            title = "Nuevo mensaje de ${sender.fullName}",
            message = (req.content ?: "").take(80), // máximo 80 caracteres
            relatedId = saved.id.toString()
        )

        return ChatMessageResponse(
            id = saved.id!!,
            senderId = sender.id!!,
            receiverId = receiver.id!!,
            content = saved.content,
            createdAt = saved.createdAt
        )
    }

    fun sendFileMessage(
        currentUserId: UUID,
        friendId: UUID,
        file: MultipartFile,
        content: String?
    ): ChatMessageResponse {
        val sender = userRepository.findById(currentUserId)
            .orElseThrow { RuntimeException("Usuario actual no encontrado") }

        val receiver = userRepository.findById(friendId)
            .orElseThrow { RuntimeException("Amigo no encontrado") }

        // 1. Subir a Cloudinary
        val url = cloudinaryService.uploadFile(file, "chat")

        // 2. Detectar tipo básico
        val mimeType = file.contentType ?: "application/octet-stream"
        val attachmentType = when {
            mimeType.startsWith("image/") -> "IMAGE"
            mimeType.startsWith("video/") -> "VIDEO"
            mimeType.startsWith("audio/") -> "AUDIO"
            else -> "FILE"
        }

        val toSave = ChatMessageEntity(
            sender = sender,
            receiver = receiver,
            content = content ?: "",
            attachmentUrl = url,
            attachmentType = attachmentType,
            createdAt = Instant.now()
        )

        val saved = chatMessageRepository.save(toSave)

        // 🔔 NOTIFICACIÓN POR MENSAJE CON ARCHIVO
        val preview = when {
            !content.isNullOrBlank() -> content.take(80)
            attachmentType == "IMAGE" -> "Te ha enviado una imagen"
            attachmentType == "VIDEO" -> "Te ha enviado un video"
            attachmentType == "AUDIO" -> "Te ha enviado un audio"
            else -> "Te ha enviado un archivo"
        }

        notificationService.createNotification(
            user = receiver,
            type = NotificationType.MESSAGE_RECEIVED,
            title = "Nuevo mensaje de ${sender.fullName}",
            message = preview,
            relatedId = saved.id.toString()
        )

        return ChatMessageResponse(
            id = saved.id!!,
            senderId = saved.sender.id!!,
            receiverId = saved.receiver.id!!,
            content = saved.content,
            createdAt = saved.createdAt,
            attachmentUrl = saved.attachmentUrl,
            attachmentType = saved.attachmentType
        )
    }
}