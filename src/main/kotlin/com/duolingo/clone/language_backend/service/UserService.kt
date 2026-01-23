package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.*
import com.duolingo.clone.language_backend.entity.UserEntity
import com.duolingo.clone.language_backend.entity.RegistrationCodeEntity
import com.duolingo.clone.language_backend.enums.Role
import com.duolingo.clone.language_backend.repository.UserRepository
import com.duolingo.clone.language_backend.repository.RegistrationCodeRepository
import com.duolingo.clone.language_backend.repository.UnitRepository
import com.duolingo.clone.language_backend.repository.UserLessonProgressRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant

import java.util.*
import java.util.regex.Pattern

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val registrationCodeRepository: RegistrationCodeRepository,
    private val unitRepository: UnitRepository,
    private val userLessonProgressRepository: UserLessonProgressRepository
) {

    private val EMAIL_REGEX =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

    private val NAME_REGEX =
        Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$")

    private val CEDULA_REGEX = Pattern.compile("^\\d{10}$")

    private fun validatePassword(password: String) {
        if (password.length < 8) {
            throw IllegalArgumentException("Contraseña muy débil: mínimo 8 caracteres")
        }

        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }

        when {
            password.length >= 12 && hasUpper && hasLower && hasDigit && hasSpecial -> {
                return
            }

            password.length >= 10 && hasUpper && hasLower && hasDigit -> {
                return
            }

            password.length >= 8 && hasLower && hasDigit -> {
                throw IllegalArgumentException(
                    "Contraseña media: agregue mayúsculas para mayor seguridad"
                )
            }

            else -> {
                throw IllegalArgumentException(
                    "Contraseña débil: use mayúsculas, números y símbolos"
                )
            }
        }
    }


    fun createNewUser(
        email: String,
        password: String,
        fullName: String,
        role: Role,
        cedula: String,
        registrationCode: String? = null,
        registeredBy: UserEntity? = null
    ): UserEntity {
        validatePassword(password)
        val cleanEmail = email.lowercase().trim()
        val cleanName = fullName.trim()
        val cleanCedula = cedula.trim()

        require(EMAIL_REGEX.matcher(cleanEmail).matches()) { "Email inválido" }
        require(NAME_REGEX.matcher(cleanName).matches()) {
            "El nombre no debe contener números ni símbolos"
        }
        require(CEDULA_REGEX.matcher(cleanCedula).matches()) {
            "La cédula debe contener exactamente 10 números"
        }

        if (userRepository.findByEmail(cleanEmail) != null) {
            throw RuntimeException("El correo ya está registrado")
        }
        if (userRepository.findByCedula(cleanCedula) != null) {
            throw RuntimeException("La cédula ya está registrada")
        }

        // --- CORRECCIÓN AQUÍ: Validar código tanto para estudiantes como para profesores con PROF- ---
        if (role == Role.STUDENT || (role == Role.TEACHER && registrationCode?.startsWith("PROF-") == true)) {
            if (registrationCode.isNullOrBlank()) {
                throw RuntimeException("El código de registro es obligatorio")
            }

            val codeEntity = registrationCodeRepository
                .findByCode(registrationCode)
                .orElseThrow { RuntimeException("Código inválido") }

            if (codeEntity.expiresAt.isBefore(Instant.now())) {
                throw RuntimeException("El código ha expirado")
            }

            if (codeEntity.usedCount >= codeEntity.maxUses) {
                throw RuntimeException("El código ya alcanzó su límite")
            }

            codeEntity.usedCount++
            registrationCodeRepository.save(codeEntity)
        }

        val newUser = UserEntity(
            email = cleanEmail,
            passwordHash = passwordEncoder.encode(password),
            fullName = cleanName,
            role = role,
            cedula = cleanCedula,
            registeredById = registeredBy?.id,
            registeredByName = registeredBy?.fullName ?: cleanName,
            registeredByRole = registeredBy?.role ?: role,

            xpTotal = 0,
            heartsCount = if (role == Role.STUDENT) 5 else 0,
            currentStreak = 0,
            lingotsCount = 100,
            hasStreakFreeze = false,
            isActive = true,
            createdAt = Instant.now(),
            lastPracticeDate = Instant.now(),
            lastHeartRefillTime = Instant.now()
        )

        return userRepository.save(newUser)
    }

    fun registerStudent(
        email: String,
        password: String,
        fullName: String,
        cedula: String,
        code: String
    ): UserEntity =
        createNewUser(
            email = email,
            password = password,
            fullName = fullName,
            role = Role.STUDENT,
            cedula = cedula,
            registrationCode = code
        )

    // ✅ CORRECCIÓN AÑADIDA: Función que faltaba
    fun registerTeacher(
        email: String,
        password: String,
        fullName: String,
        cedula: String,
        code: String
    ): UserEntity =
        createNewUser(
            email = email,
            password = password,
            fullName = fullName,
            role = Role.TEACHER,
            cedula = cedula,
            registrationCode = code
        )


    fun createAdminUser(
        email: String,
        password: String,
        fullName: String,
        cedula: String
    ): UserEntity =
        createNewUser(
            email = email,
            password = password,
            fullName = fullName,
            role = Role.ADMIN,
            cedula = cedula
        )



    fun generateRegistrationCode(
        teacherId: UUID,
        maxUses: Int = 20
    ): String {

        val code = "AULA-" + UUID.randomUUID()
            .toString()
            .substring(0, 6)
            .uppercase()

        val entity = RegistrationCodeEntity(
            code = code,
            expiresAt = Instant.now().plusSeconds(3600),
            maxUses = maxUses,
            usedCount = 0,
            createdByTeacherId = teacherId
        )

        registrationCodeRepository.save(entity)
        return code
    }

    fun bulkRegisterStudents(
        request: BulkRegisterRequest,
        registeredByUserId: UUID
    ): BulkRegisterResponse {

        val registeredBy = userRepository.findById(registeredByUserId)
            .orElseThrow { RuntimeException("Usuario registrador no encontrado") }

        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<BulkRegistrationError>()

        request.students.forEach { student ->
            try {
                createNewUser(
                    email = student.email,
                    password = student.password,
                    fullName = student.fullName,
                    role = Role.STUDENT,
                    registrationCode = request.registrationCode,
                    registeredBy = registeredBy,
                    cedula = student.cedula,
                )
                successCount++
            } catch (e: Exception) {
                failureCount++
                errors.add(
                    BulkRegistrationError(
                        student.email,
                        e.message ?: "Error desconocido"
                    )
                )
            }
        }

        return BulkRegisterResponse(
            totalProcessed = request.students.size,
            successCount = successCount,
            failureCount = failureCount,
            errors = errors
        )
    }
    fun getUserByEmail(email: String): UserEntity? {
        return userRepository.findByEmail(email.lowercase().trim())
    }

    fun verifyCredentials(email: String, password: String): Boolean {
        val user = getUserByEmail(email) ?: return false
        if (!user.isActive) {
            throw RuntimeException("Usuario desactivado")
        }
        return passwordEncoder.matches(password, user.passwordHash)
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



    @Transactional
    fun subtractHeart(userId: UUID): UserEntity {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        if (user.heartsCount > 0) {
            user.heartsCount -= 1

            if (user.heartsCount < 5 && user.lastHeartRefillTime == null) {
                user.lastHeartRefillTime = Instant.now()
            }

            return userRepository.save(user)
        }
        return user
    }

    fun refreshUserHearts(user: UserEntity): UserEntity {
        val lastRefill = user.lastHeartRefillTime ?: Instant.now()

        if (user.heartsCount >= 5) {
            user.lastHeartRefillTime = Instant.now()
            return userRepository.save(user)
        }

        val now = Instant.now()
        val secondsSinceLastRefill = now.epochSecond - lastRefill.epochSecond
        val secondsPerHeart = 300 // 5 minutos

        val heartsToRegen = (secondsSinceLastRefill / secondsPerHeart).toInt()

        if (heartsToRegen > 0) {
            val newHeartCount = (user.heartsCount + heartsToRegen).coerceAtMost(5)
            user.heartsCount = newHeartCount

            if (newHeartCount >= 5) {
                user.lastHeartRefillTime = now
            } else {
                user.lastHeartRefillTime = lastRefill.plusSeconds((heartsToRegen * secondsPerHeart).toLong())
            }
            return userRepository.save(user)
        }

        return user
    }

    fun getUserById(id: UUID): UserEntity? {
        return userRepository.findById(id).orElse(null)
    }

    fun generateTeacherInviteCode(): String {
        // Generamos el código con prefijo PROF-
        val code = "PROF-" + UUID.randomUUID().toString().substring(0, 6).uppercase()

        val entity = RegistrationCodeEntity(
            code = code,
            expiresAt = Instant.now().plusSeconds(86400), // 24 horas
            maxUses = 1,
            usedCount = 0,
            createdByTeacherId = null // Importante: null porque lo crea el Admin
        )

        return try {
            registrationCodeRepository.save(entity)
            code // Si guarda bien, retorna el string
        } catch (e: Exception) {
            println("ERROR CRÍTICO EN DB: ${e.message}")
            throw RuntimeException("Error al guardar el código en la base de datos: ${e.message}")
        }
    }

    // En UserService.kt
    fun getDetailedProgressForStudent(studentId: UUID): List<UnitProgressDTO> {
        val units = unitRepository.findAll()

        return units.map { unit ->
            UnitProgressDTO(
                id = unit.id!!,
                title = unit.title,
                lessons = unit.lessons.map { lesson ->

                    // 1. Buscamos el progreso real del alumno
                    val progress = userLessonProgressRepository.findByUserIdAndLessonId(studentId, lesson.id!!)

                    // 2. Calculamos el XP: 10 base + (aciertos * 2) solo si terminó
                    val xpCalculado = if (progress?.isCompleted == true) {
                        10 + ((progress.correctAnswers ?: 0) * 2)
                    } else {
                        0
                    }

                    // 3. Construimos el DTO con todos los parámetros requeridos
                    LessonProgressDetailDTO(
                        id = lesson.id!!,
                        title = lesson.title,
                        isCompleted = progress?.isCompleted ?: false,
                        mistakesCount = progress?.mistakesCount ?: 0,
                        correctAnswers = progress?.correctAnswers ?: 0,
                        lastPracticed = null,
                        xpEarned = xpCalculado // ✅ Aquí pasamos el valor que faltaba
                    )
                }
            )
        }
    }
}