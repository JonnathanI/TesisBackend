package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CurrentUserService(
    private val userRepository: UserRepository
) {

    fun getCurrentUserId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw RuntimeException("Usuario no autenticado")

        val name = authentication.name  // ahora puede ser UUID o email

        // 1) Intentamos tratarlo como UUID directamente (lo que está pasando ahora)
        return try {
            UUID.fromString(name)   // si es un UUID válido, listo
        } catch (ex: IllegalArgumentException) {
            // 2) Si no es UUID, lo tomamos como email (modo viejo)
            val user = userRepository.findByEmail(name)
                ?: throw RuntimeException("Usuario con email $name no encontrado")

            user.id ?: throw RuntimeException("El usuario no tiene ID asignado")
        }
    }
}
