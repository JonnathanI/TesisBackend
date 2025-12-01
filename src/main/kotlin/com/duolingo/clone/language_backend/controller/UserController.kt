package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.AvatarUpdateRequest
import com.duolingo.clone.language_backend.dto.UserProfileResponse
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userRepository: UserRepository
) {

    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal userId: String): ResponseEntity<UserProfileResponse> {
        // Convertir el ID del token (String) a UUID
        val uuid = UUID.fromString(userId)

        // Buscar al usuario en la base de datos
        val user = userRepository.findById(uuid)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        // Generar un nombre de usuario simple (ej. 'juan' desde 'juan@email.com')
        val username = user.email.substringBefore("@")

        // Construir la respuesta
        val response = UserProfileResponse(
            fullName = user.fullName,
            username = username,
            joinedAt = user.createdAt,
            totalXp = user.xpTotal,
            currentStreak = user.currentStreak,
            lingots = user.lingotsCount,
            league = "Bronce", // Lógica de ligas futura
            avatarData = user.avatarData
        )

        return ResponseEntity.ok(response)
    }
    @PostMapping("/me/avatar")
    fun updateAvatar(
        @AuthenticationPrincipal userId: String,
        @RequestBody request: AvatarUpdateRequest
    ): ResponseEntity<String> {
        val uuid = UUID.fromString(userId)
        val user = userRepository.findById(uuid).orElseThrow { RuntimeException("User not found") }

        user.avatarData = request.avatarData
        userRepository.save(user)

        return ResponseEntity.ok("Avatar actualizado")
    }
}