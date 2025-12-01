package com.duolingo.clone.language_backend.filter

import com.duolingo.clone.language_backend.repository.UserRepository
import com.duolingo.clone.language_backend.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. Obtener el encabezado de autorización
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        // 2. Extraer el token y el ID de usuario
        val jwt = authHeader.substring(7)

        // Usamos un bloque try-catch para manejar tokens inválidos
        try {
            val userId = jwtService.extractUserId(jwt)

            // 3. Verificar si el usuario ya está autenticado
            if (userId.isNotEmpty() && SecurityContextHolder.getContext().authentication == null) {

                // 4. Cargar el UserEntity desde el DB (conviertiendo el String UUID a UUID real)
                val user = userRepository.findById(UUID.fromString(userId)).orElse(null)

                // 5. Validar el token contra el usuario
                if (user != null && jwtService.isTokenValid(jwt, user)) {

                    // Crea la lista de autoridades (roles)
                    val authorities = listOf(SimpleGrantedAuthority(user.role.name))

                    // 6. Crear el objeto de autenticación
                    val authToken = UsernamePasswordAuthenticationToken(
                        user.id.toString(), // Principal: el ID del usuario
                        null,
                        authorities // Roles del usuario
                    )

                    // 7. Establecer la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        } catch (e: Exception) {
            // Logear o manejar errores de token (expiración, firma inválida)
            logger.warn("JWT inválido o expirado: ${e.message}")
            // No establecemos el contexto, la petición fallará con 403/401
        }

        filterChain.doFilter(request, response)
    }
}