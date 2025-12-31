package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.*
import com.duolingo.clone.language_backend.entity.UserEntity
import com.duolingo.clone.language_backend.entity.RegistrationCodeEntity
import com.duolingo.clone.language_backend.enums.Role
import com.duolingo.clone.language_backend.repository.UserRepository
import com.duolingo.clone.language_backend.repository.RegistrationCodeRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val registrationCodeRepository: RegistrationCodeRepository
) {

    private val EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    private val NAME_REGEX = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$")

    /**
     * Crea un nuevo usuario con validación de código de registro para estudiantes.
     */
    fun createNewUser(
        email: String,
        password: String,
        fullName: String,
        role: Role,
        registrationCode: String? = null
    ): UserEntity {
        val cleanEmail = email.lowercase().trim()
        val cleanName = fullName.trim()

        // 1. Validaciones de formato
        require(EMAIL_REGEX.matcher(cleanEmail).matches()) { "Email inválido." }
        require(NAME_REGEX.matcher(cleanName).matches()) { "Nombre contiene caracteres especiales." }

        if (userRepository.findByEmail(cleanEmail) != null) {
            throw RuntimeException("El correo ya se encuentra registrado.")
        }

        // 2. Validación de código de registro para Estudiantes
        if (role == Role.STUDENT) {
            if (registrationCode.isNullOrBlank()) {
                throw RuntimeException("El código de registro es obligatorio para estudiantes.")
            }

            val codeEntity = registrationCodeRepository.findByCode(registrationCode)
                .orElseThrow { RuntimeException("Código de registro inválido.") }

            if (codeEntity.expiresAt.isBefore(Instant.now())) {
                throw RuntimeException("Este código ya ha sido utilizado.")
            }

            // Marcamos el código como usado (invalidación)
            codeEntity.usedCount += 1
            registrationCodeRepository.save(codeEntity)
        }

        val newUser = UserEntity(
            email = cleanEmail,
            passwordHash = passwordEncoder.encode(password),
            fullName = cleanName,
            role = role,
            xpTotal = 0,
            heartsCount = if (role == Role.STUDENT) 5 else 0,
            createdAt = Instant.now(), // Se asume que es Instant en la entidad
            currentStreak = 0,
            lingotsCount = 100,
            hasStreakFreeze = false,
            isActive = true,
            lastPracticeDate = Instant.now(),
            lastHeartRefillTime = Instant.now()
        )
        return userRepository.save(newUser)
    }

    fun registerStudent(email: String, password: String, fullName: String, code: String?): UserEntity {
        return createNewUser(email, password, fullName, Role.STUDENT, code)
    }

    fun createAdminUser(email: String, password: String, fullName: String): UserEntity {
        return createNewUser(email, password, fullName, Role.ADMIN, null)
    }

    fun generateRegistrationCode(
        teacherId: UUID,
        maxUses: Int = 20
    ): String {

        require(maxUses > 0) { "El número de cupos debe ser mayor a 0" }

        val code = "AULA-" + UUID.randomUUID().toString().substring(0, 6).uppercase()

        val entity = RegistrationCodeEntity(
            code = code,
            expiresAt = Instant.now().plusSeconds(60 * 60), // ⏱️ 1 hora
            maxUses = maxUses,
            usedCount = 0,
            createdByTeacherId = teacherId
        )

        registrationCodeRepository.save(entity)
        return code
    }



    fun getUserProfile(userId: UUID): UserProfileResponse {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }

        // CORRECCIÓN: Para formatear un Instant, hay que convertirlo a ZonedDateTime
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
        val joinedDate = user.createdAt?.let {
            ZonedDateTime.ofInstant(it, ZoneId.systemDefault()).format(formatter)
        } ?: "N/A"

        return UserProfileResponse(
            fullName = user.fullName,
            username = user.email.split("@")[0],
            joinedAt = Instant.now(), // Ahora pasamos el String formateado correctamente
            totalXp = user.xpTotal,
            currentStreak = user.currentStreak,
            lingots = user.lingotsCount,
            heartsCount = user.heartsCount,
            league = "Bronce",
            avatarData = user.avatarData
        )
    }

    fun bulkRegisterStudents(request: BulkRegisterRequest): BulkRegisterResponse {
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<BulkRegistrationError>()

        request.students.forEach { studentItem ->
            try {
                // En registro masivo por el profesor, no pedimos código de validación
                createNewUser(studentItem.email, studentItem.password, studentItem.fullName, Role.STUDENT, null)
                successCount++
            } catch (e: Exception) {
                failureCount++
                errors.add(BulkRegistrationError(studentItem.email, e.message ?: "Error"))
            }
        }
        return BulkRegisterResponse(request.students.size, successCount, failureCount, errors)
    }

    fun getUserByEmail(email: String) = userRepository.findByEmail(email)
    fun verifyCredentials(email: String, password: String): Boolean {
        val user = userRepository.findByEmail(email)
        return user != null && passwordEncoder.matches(password, user.passwordHash)
    }
    fun getAllStudents(): List<StudentDataDTO> {
        return userRepository.findByRole(Role.STUDENT).map { user ->
            StudentDataDTO(
                id = user.id!!,
                fullName = user.fullName,
                email = user.email,
                xpTotal = user.xpTotal,
                heartsCount = user.heartsCount,
                lingotsCount = user.lingotsCount,
                currentStreak = user.currentStreak,
                isActive = user.isActive
            )
        }
    }
}