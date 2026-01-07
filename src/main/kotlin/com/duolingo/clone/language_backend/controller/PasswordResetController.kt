package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.PasswordResetConfirmDTO
import com.duolingo.clone.language_backend.dto.PasswordResetRequestDTO
import com.duolingo.clone.language_backend.service.PasswordResetService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth/password")
class PasswordResetController(
    private val passwordResetService: PasswordResetService
) {

    @PostMapping("/forgot")
    fun forgotPassword(
        @RequestBody dto: PasswordResetRequestDTO
    ): ResponseEntity<Map<String, String>> {

        passwordResetService.requestPasswordReset(dto.email)

        return ResponseEntity.ok(
            mapOf("message" to "Si el correo existe, se enviará un enlace")
        )
    }

    @PostMapping("/reset")
    fun resetPassword(
        @RequestBody dto: PasswordResetConfirmDTO
    ): ResponseEntity<Map<String, String>> {

        passwordResetService.resetPassword(dto.token, dto.newPassword)

        return ResponseEntity.ok(
            mapOf("message" to "Contraseña actualizada correctamente")
        )
    }
}
