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
        val path = request.servletPath

        // 🚫 RUTAS QUE NO DEBEN PASAR POR EL FILTRO JWT
        if (
            path.startsWith("/api/auth") ||          // login, register, etc.
            path.startsWith("/ws") ||                // websockets
            path.startsWith("/api/debug/fcm") ||     // 👈 nuestro endpoint de pruebas
            path.startsWith("/h2-console") ||
            path == "/error"
        ) {
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader("Authorization")

        // Si no hay Authorization o no es Bearer, simplemente dejamos pasar.
        // SecurityConfig decidirá si la ruta requiere auth o no.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt = authHeader.substring(7)

        try {
            val userId = jwtService.extractUserId(jwt)

            if (SecurityContextHolder.getContext().authentication == null) {
                val user = userRepository.findById(UUID.fromString(userId)).orElse(null)

                if (user != null && jwtService.isTokenValid(jwt, user)) {

                    if (!user.isActive) {
                        response.status = HttpServletResponse.SC_UNAUTHORIZED
                        response.writer.write("Usuario desactivado")
                        return
                    }

                    val authorities = listOf(SimpleGrantedAuthority(user.role.name))

                    val authToken = UsernamePasswordAuthenticationToken(
                        user.id.toString(),
                        null,
                        authorities
                    )

                    SecurityContextHolder.getContext().authentication = authToken
                }
            }

        } catch (e: io.jsonwebtoken.ExpiredJwtException) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Token expirado")
            return
        } catch (e: Exception) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Token inválido")
            return
        }

        filterChain.doFilter(request, response)
    }
}
