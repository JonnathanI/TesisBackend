package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.*
import com.duolingo.clone.language_backend.service.UserService
import com.duolingo.clone.language_backend.service.JwtService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
                // Si el código es correcto, llama a createAdminUser
                userService.createAdminUser(request.email, request.password, request.fullName)
            } else {
                // Si es nulo o incorrecto, crea un estudiante
                userService.registerStudent(request.email, request.password, request.fullName,request.registrationCode)
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


    // === NUEVO ENDPOINT PARA CARGA MASIVA DE ALUMNOS ===
    // Este método permite a la empresa registrar a todos los alumnos de una vez
    @PostMapping("/register-bulk")
    fun registerBulk(@RequestBody request: BulkRegisterRequest): ResponseEntity<BulkRegisterResponse> {
        // Llamamos al servicio que procesa la lista uno por uno
        val result = userService.bulkRegisterStudents(request)
        return ResponseEntity.ok(result)
    }


    // POST /api/auth/login
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        if (userService.verifyCredentials(request.email, request.password)) {
            val user = userService.getUserByEmail(request.email)!!
            val token = jwtService.generateToken(user)

            val response = AuthResponse(
                token = token,
                userId = user.id.toString(),
                role = user.role.name,
                fullName = user.fullName
            )
            return ResponseEntity.ok(response)
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }
}