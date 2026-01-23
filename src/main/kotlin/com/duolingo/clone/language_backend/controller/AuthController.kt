package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.*
import com.duolingo.clone.language_backend.entity.UserEntity
import com.duolingo.clone.language_backend.service.UserService
import com.duolingo.clone.language_backend.service.JwtService
import com.duolingo.clone.language_backend.repository.RegistrationCodeRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val registrationCodeRepository: RegistrationCodeRepository
) {

    // --- CONFIGURACIÓN DE SEGURIDAD ---
    private val ADMIN_SECRET_CODE = "supersecreto123"

    /**
     * POST /api/auth/register
     * Registro inteligente: Detecta el rol según el código proporcionado.
     */
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        try {
            println("Intento de registro para: ${request.email}")

            val user: UserEntity = when {
                // 1️⃣ CASO: ADMINISTRADOR (Usa el código secreto fijo)
                !request.adminCode.isNullOrBlank() && request.adminCode == ADMIN_SECRET_CODE -> {
                    println("-> Registrando como ADMIN")
                    userService.createAdminUser(
                        request.email, request.password, request.fullName, request.cedula
                    )
                }

                // 2️⃣ CASO: PROFESOR (Usa un código generado que empieza con PROF-)
                !request.registrationCode.isNullOrBlank() && request.registrationCode!!.startsWith("PROF-") -> {
                    println("-> Validando código de invitación para PROFESOR: ${request.registrationCode}")

                    val codeEntity = registrationCodeRepository.findByCode(request.registrationCode!!)
                        .orElseThrow { IllegalArgumentException("Código de profesor inválido o inexistente") }

                    if (codeEntity.usedCount >= codeEntity.maxUses) {
                        throw IllegalArgumentException("Este código de invitación ya ha sido utilizado")
                    }

                    val teacher = userService.registerTeacher(
                        request.email, request.password, request.fullName, request.cedula, request.registrationCode
                    )

                    // Marcar código como usado
                    codeEntity.usedCount++
                    registrationCodeRepository.save(codeEntity)
                    teacher
                }

                // 3️⃣ CASO: ESTUDIANTE (Usa código de aula AULA- o cualquier otro no filtrado arriba)
                else -> {
                    println("-> Registrando como STUDENT con código de aula: ${request.registrationCode}")
                    if (request.registrationCode.isNullOrBlank()) {
                        throw IllegalArgumentException("El código de registro es obligatorio para estudiantes")
                    }
                    userService.registerStudent(
                        request.email, request.password, request.fullName, request.cedula, request.registrationCode!!
                    )
                }
            }

            // Generación de respuesta exitosa
            val token = jwtService.generateToken(user)
            return ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponse(
                    token = token,
                    userId = user.id.toString(),
                    role = user.role.name,
                    fullName = user.fullName
                )
            )

        } catch (e: Exception) {
            println("Error en Registro: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    /**
     * POST /api/auth/admin/generate-teacher-code
     * Endpoint exclusivo para que el ADMIN genere códigos de invitación para PROFESORES.
     */
    @PostMapping("/admin/generate-teacher-code")
    fun generateTeacherCode(): ResponseEntity<Any> {
        return try {
            val nuevoCodigo = userService.generateTeacherInviteCode()
            // Retornamos un MAPA para que se convierta en JSON {"code": "PROF-XXXX"}
            ResponseEntity.ok(mapOf("code" to nuevoCodigo))
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla, devolvemos un JSON con el error en lugar de nada
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to (e.message ?: "Error interno del servidor")))
        }
    }

    /**
     * POST /api/auth/login
     */
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        // Validación de campos
        if (request.email.isBlank() || request.password.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("message" to "Debe llenar todos los campos"))
        }

        val user = userService.getUserByEmail(request.email)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to "Usuario no registrado"))

        if (!user.isActive) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("message" to "Usuario desactivado"))
        }

        if (!userService.verifyCredentials(request.email, request.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to "Contraseña incorrecta"))
        }

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
}