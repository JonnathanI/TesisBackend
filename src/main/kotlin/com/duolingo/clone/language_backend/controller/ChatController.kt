package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.ChatMessageResponse
import com.duolingo.clone.language_backend.dto.SendMessageRequest
import com.duolingo.clone.language_backend.repository.ChatMessageRepository
import com.duolingo.clone.language_backend.service.ChatService
import com.duolingo.clone.language_backend.service.CurrentUserService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService,
    private val currentUserService: CurrentUserService
) {

    @GetMapping("/{friendId}")
    fun getConversation(@PathVariable friendId: UUID): List<ChatMessageResponse> {
        val currentUserId = currentUserService.getCurrentUserId()
        return chatService.getConversation(currentUserId, friendId)
    }

    @PostMapping("/{friendId}")
    fun sendMessage(
        @PathVariable friendId: UUID,
        @RequestBody req: SendMessageRequest
    ): ChatMessageResponse {
        val currentUserId = currentUserService.getCurrentUserId()
        return chatService.sendMessage(currentUserId, friendId, req)
    }

    @PostMapping("/{friendId}/file", consumes = ["multipart/form-data"])
    fun sendFile(
        @PathVariable friendId: UUID,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("content", required = false) content: String?
    ): ChatMessageResponse {
        val currentUserId = currentUserService.getCurrentUserId()
        return chatService.sendFileMessage(currentUserId, friendId, file, content)
    }
}
