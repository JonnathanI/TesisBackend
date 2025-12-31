package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.AuthResponse
import com.duolingo.clone.language_backend.dto.RegisterRequest
import com.duolingo.clone.language_backend.service.JwtService
import com.duolingo.clone.language_backend.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/init") // Una ruta separada de /api/auth
class AdminInitController(
    private val userService: UserService,
    private val jwtService: JwtService
) {
    // ESTE ENDPOINT DEBE SER ELIMINADO/COMENTADO DESPUÉS DE LA INICIALIZACIÓN
    @PostMapping("/admin")
    fun createAdmin(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        try {
            val user = userService.createAdminUser(request.email, request.password, request.fullName, request.cedula)

            val token = jwtService.generateToken(user)

            val response = AuthResponse(
                token = token,
                userId = user.id.toString(),
                role = user.role.name, // Esto será 'ADMIN'
                fullName = user.fullName
            )
            return ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
    }
}