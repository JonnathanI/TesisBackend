package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.toDto
import com.duolingo.clone.language_backend.entity.NotificationEntity
import com.duolingo.clone.language_backend.entity.NotificationType
import com.duolingo.clone.language_backend.entity.UserEntity
import com.duolingo.clone.language_backend.repository.FcmTokenRepository
import com.duolingo.clone.language_backend.repository.NotificationRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val fcmTokenRepository: FcmTokenRepository,
    private val fcmService: FcmService
) {

    fun createNotification(
        user: UserEntity,
        type: NotificationType,
        title: String,
        message: String,
        relatedId: String? = null
    ): NotificationEntity {

        val notification = NotificationEntity(
            user = user,
            type = type,
            title = title,
            message = message,
            relatedId = relatedId
        )

        val saved = notificationRepository.save(notification)

        // 1) Notificación interna por WebSocket (campanita)
        messagingTemplate.convertAndSendToUser(
            user.id.toString(),
            "/queue/notifications",
            saved.toDto()
        )

        // 2) Notificación push por FCM (segundo plano)
        val tokens = fcmTokenRepository.findAllByUserAndActiveIsTrue(user)

        if (tokens.isEmpty()) {
            println("ℹ️ Usuario ${user.email} no tiene tokens FCM activos")
        } else {
            // info extra para el front (puedes usarla en firebase-messaging-sw.js)
            val dataPayload = mapOf(
                "type" to type.name,
                "relatedId" to (relatedId ?: ""),
                "userId" to user.id.toString()
            )

            tokens.forEach { fcmToken ->
                try {
                    fcmService.sendPush(
                        token = fcmToken.token,
                        title = title,
                        body = message,
                        data = dataPayload
                    )
                } catch (e: Exception) {
                    println("❌ Error enviando FCM a ${fcmToken.token}: ${e.message}")
                }
            }
        }

        return saved
    }

    fun markAsRead(id: UUID, currentUser: UserEntity) {
        val notif = notificationRepository.findById(id).orElseThrow()
        if (notif.user.id != currentUser.id) {
            throw IllegalAccessException("Notificación no pertenece al usuario")
        }
        notif.read = true
        notificationRepository.save(notif)
    }

    fun getUserNotifications(currentUser: UserEntity): List<NotificationEntity> =
        notificationRepository.findByUserOrderByCreatedAtDesc(currentUser)

    fun getUnreadCount(currentUser: UserEntity): Long =
        notificationRepository.countByUserAndReadIsFalse(currentUser)
}