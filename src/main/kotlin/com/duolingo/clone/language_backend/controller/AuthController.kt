package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.*
import com.duolingo.clone.language_backend.service.UserService
import com.duolingo.clone.language_backend.service.JwtService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val jwtService: JwtService
) {

    // --- ¡DEFINE TU CÓDIGO SECRETO AQUÍ! ---
    private val ADMIN_SECRET_CODE = "supersecreto123" // ¡Cámbialo!

    // POST /api/auth/register
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        try {
            println("Recibido registro para: ${request.email} con código: ${request.registrationCode}")
            // --- ¡NUEVA LÓGICA DE ROLES! ---
            val user = if (request.adminCode == ADMIN_SECRET_CODE) {
                userService.createAdminUser(
                    request.email,
                    request.password,
                    request.fullName,
                    request.cedula
                )
            } else {
                userService.registerStudent(
                    request.email,
                    request.password,
                    request.fullName,
                    request.cedula,
                    request.registrationCode!!
                )
            }

            // --- ---

            val token = jwtService.generateToken(user)

            val response = AuthResponse(
                token = token,
                userId = user.id.toString(),
                role = user.role.name, // "STUDENT" o "ADMIN"
                fullName = user.fullName
            )
            return ResponseEntity.status(HttpStatus.CREATED).body(response)

        } catch (e: IllegalArgumentException) {
            // Maneja el error si el email ya existe
            return ResponseEntity.badRequest().build()
        }
    }


    @PostMapping("/register-bulk")
    fun registerBulk(
        @AuthenticationPrincipal userId: String,
        @RequestBody request: BulkRegisterRequest
    ): ResponseEntity<BulkRegisterResponse> {

        val result = userService.bulkRegisterStudents(
            request = request,
            registeredByUserId = UUID.fromString(userId)
        )

        return ResponseEntity.ok(result)
    }



    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {

        // 1️⃣ Campos obligatorios
        if (request.email.isBlank() || request.password.isBlank()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Debe llenar todos los campos"))
        }

        // 2️⃣ Formato de correo
        val emailRegex =
            Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

        if (!emailRegex.matches(request.email)) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Formato de correo inválido"))
        }

        val user = userService.getUserByEmail(request.email)
            ?: return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("message" to "Usuario no registrado"))

        // 3️⃣ Usuario activo
        if (!user.isActive) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(mapOf("message" to "Usuario desactivado"))
        }

        // 4️⃣ Contraseña incorrecta
        if (!userService.verifyCredentials(request.email, request.password)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("message" to "Contraseña incorrecta"))
        }

        // 5️⃣ Login exitoso → generar token
        val token = jwtService.generateToken(user)

        return ResponseEntity.ok(
            AuthResponse(
                token = token,
                userId = user.id.toString(),
                role = user.role.name,
                fullName = user.fullName
            )
        )
    }


}