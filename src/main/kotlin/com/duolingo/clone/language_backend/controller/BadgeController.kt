package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.BadgeDTO
import com.duolingo.clone.language_backend.repository.UserRepository
import com.duolingo.clone.language_backend.service.BadgeService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/badges")
class BadgeController(
    private val badgeService: BadgeService,
    private val userRepository: UserRepository
) {

    // 🔹 Correcto endpoint: /api/badges/me
    @GetMapping("/me")
    fun getMyBadges(authentication: Authentication): List<BadgeDTO> {

        val userId = authentication.name  // UUID del usuario desde el JWT

        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
            }

        return badgeService.getBadgesOfUser(user.id!!)
    }

    // 🔹 Ver insignias de otro usuario
    @GetMapping("/user/{userId}")
    fun getUserBadges(@PathVariable userId: String): List<BadgeDTO> {
        return badgeService.getBadgesOfUser(UUID.fromString(userId))
    }

    @GetMapping("/all")
    fun getAllBadges(): List<BadgeDTO> {
        return badgeService.getAllBadges()
    }

}
