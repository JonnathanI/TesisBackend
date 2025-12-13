package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.AvatarUpdateRequest
import com.duolingo.clone.language_backend.dto.LeaderboardEntryDTO
import com.duolingo.clone.language_backend.dto.UserProfileResponse
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID
import java.time.LocalDateTime // Necesitas este import para el campo

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userRepository: UserRepository
) {

    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal userId: String): ResponseEntity<UserProfileResponse> {
        val uuid = UUID.fromString(userId)
        val user = userRepository.findById(uuid)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        val username = user.email.substringBefore("@")

        // Asegúrate que UserProfileResponse tiene totalXp como Long/Number
        // El error `totalXp = user.xpTotal` implica que user.xpTotal es Long y DTO espera Int.
        // Si tu DTO (UserProfileResponse) usa Long, no hay problema. Si usa Int, es un problema en el DTO.
        // Asumo que el DTO espera Long, lo cual es correcto.
        val response = UserProfileResponse(
            fullName = user.fullName,
            username = username,
            joinedAt = user.createdAt, // Corregido: Referencia al campo en UserEntity
            totalXp = user.xpTotal,    // Inferred type Long (de UserEntity)
            currentStreak = user.currentStreak,
            lingots = user.lingotsCount,
            heartsCount  = user.heartsCount,
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
}