package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.AvatarUpdateRequest
import com.duolingo.clone.language_backend.dto.LeaderboardEntryDTO
import com.duolingo.clone.language_backend.dto.UserProfileResponse
import com.duolingo.clone.language_backend.repository.UserRepository
import com.duolingo.clone.language_backend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID
import java.time.LocalDateTime // Necesitas este import para el campo

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userRepository: UserRepository,
    private val userService: UserService
) {

    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal userId: String): ResponseEntity<UserProfileResponse> {
        val uuid = UUID.fromString(userId)
        // Cambiamos a 'var' para poder actualizar el usuario con los corazones nuevos
        var user = userRepository.findById(uuid)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        // 1. Ejecutamos la lógica de regeneración antes de enviar los datos
        user = userService.refreshUserHearts(user)

        val username = user.email.substringBefore("@")

        // 2. Calculamos el tiempo para el siguiente corazón (5 minutos = 300 segundos)
        // Usamos el operador elvis (?:) por si lastHeartRefillTime es nulo
        val lastRefill = user.lastHeartRefillTime ?: Instant.now()
        val nextRegen = if (user.heartsCount < 5) {
            lastRefill.plusSeconds(300)
        } else {
            null
        }

        val response = UserProfileResponse(
            fullName = user.fullName,
            username = username,
            joinedAt = user.createdAt,
            totalXp = user.xpTotal,
            currentStreak = user.currentStreak,
            lingots = user.lingotsCount,
            heartsCount = user.heartsCount,
            nextHeartRegenTime = nextRegen, // Ahora sí está definido
            league = "Bronce",
            avatarData = user.avatarData
        )

        return ResponseEntity.ok(response)
    }

    // ... (Resto de UserController) ...
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

    @GetMapping("/leaderboard/global")
    fun getGlobalLeaderboard(): ResponseEntity<List<LeaderboardEntryDTO>> {
        val topUsers = userRepository.findTop5ByOrderByXpTotalDesc()

        val leaderboard = topUsers.mapIndexed { index, user ->
            LeaderboardEntryDTO(
                userId = user.id!!,
                fullName = user.fullName,
                // Usamos toLong() por si acaso el DTO lo requiere explícitamente
                xpTotal = user.xpTotal,
                position = index + 1
            )
        }
        return ResponseEntity.ok(leaderboard)
    }
    @PostMapping("/me/subtract-heart")
    fun subtractHeart(): ResponseEntity<UserProfileResponse> {
        val authentication = SecurityContextHolder.getContext().authentication

        // El .name contiene el UUID (d8413be0...) según tus logs
        val userIdString = authentication.name
        val userId = UUID.fromString(userIdString)

        // 1. Buscamos al usuario usando la nueva función
        val user = userService.getUserById(userId)
            ?: throw RuntimeException("Usuario no encontrado con ID: $userId")

        // 2. Restamos la vida en la base de datos
        val userWithLessHearts = userService.subtractHeart(user.id!!)

        // 3. Calculamos si recuperó vidas por tiempo
        val updatedUser = userService.refreshUserHearts(userWithLessHearts)

        // 4. Devolvemos la respuesta al Frontend
        val response = UserProfileResponse(
            fullName = updatedUser.fullName,
            username = updatedUser.email,
            joinedAt = updatedUser.createdAt,
            totalXp = updatedUser.xpTotal.toLong(),
            currentStreak = updatedUser.currentStreak,
            lingots = updatedUser.lingotsCount,
            heartsCount = updatedUser.heartsCount,
            nextHeartRegenTime = updatedUser.lastHeartRefillTime?.plusSeconds(300),
            league = "Bronce",
            avatarData = null
        )

        return ResponseEntity.ok(response)
    }
}