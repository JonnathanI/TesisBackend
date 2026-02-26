package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.*
import com.duolingo.clone.language_backend.entity.DailySqlChallengeEntity
import com.duolingo.clone.language_backend.entity.UserEntity
import com.duolingo.clone.language_backend.entity.RegistrationCodeEntity
import com.duolingo.clone.language_backend.entity.UserRelationship
import com.duolingo.clone.language_backend.enums.Role
import com.duolingo.clone.language_backend.repository.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val registrationCodeRepository: RegistrationCodeRepository,
    private val unitRepository: UnitRepository,
    private val userLessonProgressRepository: UserLessonProgressRepository,
    private val userRelationshipRepository: UserRelationshipRepository,
    private val dailySqlChallengeRepository: DailySqlChallengeRepository
) {
    private val zone = ZoneId.of("America/Guayaquil")
    private val mapper = jacksonObjectMapper()
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
        // 1. Validaciones de formato
        validatePassword(password)
        val cleanEmail = email.lowercase().trim()
        val cleanName = fullName.trim()
        val cleanCedula = cedula.trim()

        require(EMAIL_REGEX.matcher(cleanEmail).matches()) { "Email inválido" }
        require(NAME_REGEX.matcher(cleanName).matches()) { "El nombre no debe contener números ni símbolos" }
        require(CEDULA_REGEX.matcher(cleanCedula).matches()) { "La cédula debe contener exactamente 10 números" }

        // 2. Validaciones de unicidad
        if (userRepository.findByEmail(cleanEmail) != null) {
            throw RuntimeException("El correo ya está registrado")
        }
        if (userRepository.findByCedula(cleanCedula) != null) {
            throw RuntimeException("La cédula ya está registrada")
        }

        // 3. Lógica de Códigos de Registro (Estudiantes o Profesores con código PROF-)
        // Un administrador NO necesita código para registrar a otros,
        // pero un usuario que se registra solo (auto-registro) SÍ.
        val isAutoRegistration = registeredBy == null

        if (isAutoRegistration || !registrationCode.isNullOrBlank()) {
            validarYConsumirCodigo(registrationCode, role)
        }

        // 4. Creación de la Entidad
        val newUser = UserEntity(
            email = cleanEmail,
            passwordHash = passwordEncoder.encode(password),
            fullName = cleanName,
            role = role,
            cedula = cleanCedula,

            // AUDITORÍA: Si hay un registrador (Admin), usamos sus datos.
            // Si no, el usuario se registró a sí mismo.
            registeredById = registeredBy?.id,
            registeredByName = registeredBy?.fullName ?: "Sistema (Auto-registro)",
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

    /**
     * Función auxiliar para no ensuciar el flujo principal
     */
    private fun validarYConsumirCodigo(registrationCode: String?, role: Role) {
        // Si es estudiante o un profesor registrándose con código
        if (role == Role.STUDENT || (role == Role.TEACHER && registrationCode?.startsWith("PROF-") == true)) {
            if (registrationCode.isNullOrBlank()) {
                throw RuntimeException("El código de registro es obligatorio para este rol")
            }

            val codeEntity = registrationCodeRepository
                .findByCode(registrationCode)
                .orElseThrow { RuntimeException("Código inválido: $registrationCode") }

            if (codeEntity.expiresAt.isBefore(Instant.now())) {
                throw RuntimeException("El código $registrationCode ha expirado")
            }

            if (codeEntity.usedCount >= codeEntity.maxUses) {
                throw RuntimeException("El código $registrationCode ya alcanzó su límite de usos")
            }

            // Consumir el código
            codeEntity.usedCount++
            registrationCodeRepository.save(codeEntity)
        }
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

    fun bulkRegisterUsers(
        request: BulkRegisterRequest,
        registeredByUserId: UUID
    ): BulkRegisterResponse {

        val registeredBy = userRepository.findById(registeredByUserId)
            .orElseThrow { RuntimeException("Usuario registrador no encontrado") }

        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<BulkRegistrationError>()

        // 🔍 Log de depuración
        println("👉 [BULK] total=${request.users.size}, role=${request.roleToAssign}, code='${request.registrationCode}'")

        // Si el código NO está vacío, lo validamos UNA sola vez
        val normalizedCode = request.registrationCode.trim()
        if (normalizedCode.isNotEmpty()) {
            try {
                validateCodeForRole(normalizedCode, request.roleToAssign)
            } catch (e: Exception) {
                // Si el código entero es inválido, marcamos todos como error
                val msg = "Código inválido para rol ${request.roleToAssign}: ${e.message}"
                println("❌ [BULK] $msg")

                return BulkRegisterResponse(
                    totalProcessed = request.users.size,
                    successCount = 0,
                    failureCount = request.users.size,
                    errors = request.users.map { u ->
                        BulkRegistrationError(
                            email = u.email,
                            message = msg
                        )
                    }
                )
            }
        }

        request.users.forEach { userItem ->
            try {
                val finalPassword = userItem.password?.takeIf { it.isNotBlank() } ?: "Temporal123!"

                createNewUser(
                    email = userItem.email,
                    password = finalPassword,
                    fullName = userItem.fullName,
                    role = request.roleToAssign,
                    registrationCode = normalizedCode.ifEmpty { null }, // 👈 si está vacío, mandamos null
                    registeredBy = registeredBy,
                    cedula = userItem.cedula,
                )

                successCount++
            } catch (e: Exception) {
                failureCount++
                val msg = e.message ?: "Error desconocido"
                println("❌ [BULK] Error al crear ${userItem.email}: $msg")
                errors.add(BulkRegistrationError(userItem.email, msg))
            }
        }

        println("✅ [BULK] success=$successCount, failure=$failureCount")

        return BulkRegisterResponse(
            totalProcessed = request.users.size,
            successCount = successCount,
            failureCount = failureCount,
            errors = errors
        )
    }


    // Función auxiliar para seguridad
    private fun validateCodeForRole(code: String, role: Role) {
        if (role == Role.STUDENT && !code.startsWith("AULA-")) {
            throw RuntimeException("Código de aula inválido para estudiantes")
        }
        if (role == Role.TEACHER && !code.startsWith("PROF-")) {
            throw RuntimeException("Código de administrador inválido para profesores")
        }
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
    // Cambia el nombre de la función o su retorno en UserService.kt
    fun getDetailedProgressForStudent(studentId: UUID): DetailedStudentProgressDTO {
        // 1. Buscamos al usuario para obtener sus datos personales
        val user = userRepository.findById(studentId)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        val units = unitRepository.findAll()

        // 2. Mapeamos las unidades (tu lógica actual)
        val unitsProgress = units.map { unit ->
            UnitProgressDTO(
                id = unit.id!!,
                title = unit.title,
                lessons = unit.lessons.map { lesson ->
                    val progress = userLessonProgressRepository.findByUserIdAndLessonId(studentId, lesson.id!!)
                    val xpCalculado = if (progress?.isCompleted == true) {
                        10 + ((progress.correctAnswers ?: 0) * 2)
                    } else {
                        0
                    }

                    LessonProgressDetailDTO(
                        id = lesson.id!!,
                        title = lesson.title,
                        isCompleted = progress?.isCompleted ?: false,
                        mistakesCount = progress?.mistakesCount ?: 0,
                        correctAnswers = progress?.correctAnswers ?: 0,
                        lastPracticed = null,
                        xpEarned = xpCalculado
                    )
                }
            )
        }

        // 3. Retornamos el DTO Completo con los datos del perfil
        // ... dentro de getDetailedProgressForStudent ...

        return DetailedStudentProgressDTO(
            fullName = user.fullName,
            username = user.email.split("@")[0],
            avatarData = user.avatarData,
            totalXp = user.xpTotal.toInt(),
            currentStreak = user.currentStreak.toInt(),
            units = unitsProgress
        )
    }

    // En UserService.kt
    fun searchUsers(query: String, currentUserId: UUID): List<StudentDataDTO> {
        return userRepository.searchByNameOrEmail(query)
            .filter { it.id != currentUserId } // Aquí el "it" ya funcionará
            .map { user ->
                StudentDataDTO(
                    id = user.id!!,
                    fullName = user.fullName,
                    email = user.email,
                    xpTotal = user.xpTotal,
                    currentStreak = user.currentStreak,
                    heartsCount = user.heartsCount,
                    lingotsCount = user.lingotsCount,
                    isActive = user.isActive,
                    // ... completar campos del DTO
                )
            }
    }

    @Transactional
    fun followUser(followerId: UUID, followedId: UUID) {
        // 1. Evitar seguirse a sí mismo
        if (followerId == followedId) {
            throw RuntimeException("No puedes seguirte a ti mismo")
        }

        // 2. Verificar si ya existe la relación
        if (userRelationshipRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            throw RuntimeException("Ya sigues a este usuario")
        }

        // 3. Buscar las entidades
        val follower = userRepository.findById(followerId)
            .orElseThrow { RuntimeException("Seguidor no encontrado") }
        val followed = userRepository.findById(followedId)
            .orElseThrow { RuntimeException("Usuario a seguir no encontrado") }

        // 4. Guardar la relación
        val relationship = UserRelationship(
            follower = follower,
            followed = followed
        )

        userRelationshipRepository.save(relationship)
    }

    fun getFollowedUsers(userId: UUID): List<StudentDataDTO> {
        return userRelationshipRepository.findByFollowerId(userId).map { relationship ->
            val friend = relationship.followed
            StudentDataDTO(
                id = friend.id!!,
                fullName = friend.fullName,
                email = friend.email,
                xpTotal = friend.xpTotal,
                heartsCount = friend.heartsCount,
                lingotsCount = friend.lingotsCount,
                currentStreak = friend.currentStreak,
                isActive = friend.isActive,
                avatarData = friend.avatarData
            )
        }
    }

    @Transactional
    fun acceptFriendRequest(senderId: UUID, currentUserId: UUID) {
        // 1. Buscamos la solicitud pendiente
        val relationship = userRelationshipRepository.findByFollowerIdAndFollowedId(senderId, currentUserId)
            ?: throw RuntimeException("No se encontró la solicitud de amistad")

        // 2. Cambiamos el estado a aceptado
        relationship.status = "ACCEPTED"
        userRelationshipRepository.save(relationship)

        // 3. CREAMOS LA RELACIÓN INVERSA (Para que ambos se vean como amigos)
        val inverseRelationship = UserRelationship(
            follower = relationship.followed, // Yo
            followed = relationship.follower, // El que me agregó
            status = "ACCEPTED"
        )
        userRelationshipRepository.save(inverseRelationship)
    }

    fun getPendingRequests(userId: UUID): List<StudentDataDTO> {
        // Buscamos relaciones donde el usuario actual es el 'followed' y están pendientes
        return userRelationshipRepository.findByFollowedIdAndStatus(userId, "PENDING").map { rel ->
            val sender = rel.follower // El que envió la solicitud
            StudentDataDTO(
                id = sender.id!!,
                fullName = sender.fullName,
                email = sender.email,
                xpTotal = sender.xpTotal,
                currentStreak = sender.currentStreak,
                heartsCount = sender.heartsCount,
                lingotsCount = sender.lingotsCount,
                isActive = sender.isActive
            )
        }
    }

    fun updateUser(id: UUID, request: UpdateUserRequest): UserEntity {
        val user = userRepository.findById(id)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        user.fullName = request.fullName
        user.email = request.email
        user.cedula = request.cedula

        return userRepository.save(user)
    }

    fun getPerfectLessonsCount(userId: UUID): Int {
        return userLessonProgressRepository.countPerfectLessons(userId).toInt()
    }

    fun getDailySqlChallenge(userId: UUID): Pair<String, String> {
        // 1. Cargamos todos los retos desde la tabla
        val challenges = dailySqlChallengeRepository.findAll()

        // 2. Si no hay nada en la tabla, devolvemos un fallback
        if (challenges.isEmpty()) {
            return "Practica libre de inglés" to "Realiza cualquier lección hoy 🎧📚"
        }

        // 3. Hacemos que el reto dependa del usuario + día (fijo 24h)
        val today = LocalDate.now(zone)
        val seed = kotlin.math.abs(userId.hashCode() + today.toEpochDay().toInt())
        val index = (seed % challenges.size).toInt()

        val challenge = challenges[index]

        return challenge.title to challenge.snippet
    }
/*
    fun toSingleChallengeDTO(
        challenge: DailySqlChallengeEntity,
        stats: DailyStats
    ): SingleChallengeDTO {

        val progress = when (challenge.type.uppercase()) {
            "XP"       -> stats.dailyXp
            "TIME"     -> stats.dailyMinutes
            "PERFECT"  -> stats.perfectLessons
            "LESSONS"  -> stats.lessonsToday
            "UNITS"    -> stats.unitsPracticedToday
            "STREAK"   -> stats.currentStreak
            "LINGS"    -> stats.lingotsGainedToday
            "HEARTS"   -> stats.heartsCount
            "CORRECT"  -> stats.correctAnswersToday
            else       -> 0
        }

        val target = (challenge.goalValue ?: 1).coerceAtLeast(1)

        return SingleChallengeDTO(
            id = (challenge.id ?: 0L).toInt(),
            type = challenge.type,
            title = challenge.title,
            progress = progress.coerceAtMost(target),
            total = target
        )
    }*/
}