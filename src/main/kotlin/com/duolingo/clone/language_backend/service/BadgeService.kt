package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.BadgeDTO
import com.duolingo.clone.language_backend.entity.UserBadgeEntity
import com.duolingo.clone.language_backend.repository.BadgeRepository
import com.duolingo.clone.language_backend.repository.UserBadgeRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*
@Service
class BadgeService(
    private val badgeRepo: BadgeRepository,
    private val userBadgeRepo: UserBadgeRepository,
    private val userRepository: UserRepository  // 👈 AGREGA ESTO
) {

    fun giveBadgeIfNotOwned(userId: UUID, badgeCode: String) {
        val badge = badgeRepo.findByCode(badgeCode) ?: return

        val alreadyHas = userBadgeRepo.existsByUserIdAndBadgeId(userId, badge.id!!)
        if (alreadyHas) return

        // ✅ OBTENER usuario real
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        // Guardar la medalla
        userBadgeRepo.save(
            UserBadgeEntity(
                user = user,
                badge = badge
            )
        )
    }

    fun getBadgesOfUser(userId: UUID): List<BadgeDTO> {
        return userBadgeRepo.findAllByUserId(userId).map {
            BadgeDTO(
                id = it.id.toString(),
                code = it.badge.code,
                title = it.badge.title,
                description = it.badge.description,
                earnedAt = it.earnedAt.time
            )
        }
    }

    fun getAllBadges(): List<BadgeDTO> {
        return badgeRepo.findAll().map {
            BadgeDTO(
                id = it.id.toString(),
                code = it.code,
                title = it.title,
                description = it.description,
                earnedAt = null  // porque no está obtenida
            )
        }
    }

}
