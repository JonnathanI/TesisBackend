package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.NotificationDto
import com.duolingo.clone.language_backend.dto.toDto
import com.duolingo.clone.language_backend.service.NotificationService
import com.duolingo.clone.language_backend.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import com.duolingo.clone.language_backend.repository.UserRepository
import com.duolingo.clone.language_backend.service.FcmTokenService


@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val fcmTokenService: FcmTokenService
) {

    @GetMapping
    fun list(@AuthenticationPrincipal userId: String): List<NotificationDto> {
        val user = userService.getUserById(UUID.fromString(userId))
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado")

        return notificationService
            .getUserNotifications(user)
            .map { it.toDto() }
    }

    @GetMapping("/unread-count")
    fun unreadCount(@AuthenticationPrincipal userId: String): Long {
        val user = userService.getUserById(UUID.fromString(userId))
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado")

        return notificationService.getUnreadCount(user)
    }

    @PostMapping("/{id}/read")
    fun markAsRead(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: UUID
    ) {
        val user = userService.getUserById(UUID.fromString(userId))
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado")

        notificationService.markAsRead(id, user)
    }

    @PostMapping("/register-token")
    fun registerToken(
        @AuthenticationPrincipal userId: String,
        @RequestBody body: Map<String, String>
    ) {
        val token = body["token"] ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Falta el token"
        )

        val uuid = UUID.fromString(userId)
        fcmTokenService.registerToken(uuid, token)
    }

    @PostMapping("/test-push")
    fun sendTestPush(@AuthenticationPrincipal userId: String) {
        val uuid = UUID.fromString(userId)

        val token = fcmTokenService.getLastActiveTokenForUser(uuid)
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El usuario no tiene token FCM registrado"
            )

        notificationService.sendTestPush(uuid, token)
    }
}