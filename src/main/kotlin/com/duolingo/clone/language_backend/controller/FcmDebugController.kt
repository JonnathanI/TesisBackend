package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.service.FcmService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/debug/fcm")
class FcmDebugController(
    private val fcmService: FcmService
) {

    data class TestRequest(
        val token: String
    )

    @PostMapping("/test")
    fun sendTest(@RequestBody body: TestRequest): ResponseEntity<String> {
        if (body.token.isBlank()) {
            return ResponseEntity.badRequest().body("Token vacío")
        }

        fcmService.sendPush(
            token = body.token,
            title = "Test desde backend ✅",
            body = "Si ves esto, las notificaciones BG funcionan 😎",
            data = mapOf(
                "type" to "MESSAGE_RECEIVED",
                "message" to "Hola desde el servidor"
            )
        )

        return ResponseEntity.ok("Notificación enviada (revisa el dispositivo)")
    }
}