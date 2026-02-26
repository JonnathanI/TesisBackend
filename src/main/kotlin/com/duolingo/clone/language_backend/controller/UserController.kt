package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.*
import com.duolingo.clone.language_backend.enums.Role
import com.duolingo.clone.language_backend.repository.DailySqlChallengeRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import com.duolingo.clone.language_backend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val dailySqlChallengeRepository: DailySqlChallengeRepository
) {

    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal userId: String): ResponseEntity<UserProfileResponse> {
        val uuid = UUID.fromString(userId)
        // Cambiamos a 'var' para poder actualizar el usuario con los corazones nuevos
        var user = userRepository.findById(uuid)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        // 1. Ejecutamos la lógica de regeneración antes de enviar los datos
        user = userService.refreshUserHearts(user)

        val username = user.email.substringBefore("@")

        // 2. Calculamos el tiempo para el siguiente corazón (5 minutos = 300 segundos)
        // Usamos el operador elvis (?:) por si lastHeartRefillTime es nulo
        val lastRefill = user.lastHeartRefillTime ?: Instant.now()
        val nextRegen = if (user.heartsCount < 5) {
            lastRefill.plusSeconds(300)
        } else {
            null
        }

        val response = UserProfileResponse(
            fullName = user.fullName,
            username = username,
            joinedAt = user.createdAt,
            totalXp = (user.xpTotal ?: 0L).toInt(),     // 👈 aquí
            currentStreak = (user.currentStreak ?: 0).toInt(),
            lingots = (user.lingotsCount ?: 0).toInt(),
            heartsCount = user.heartsCount,
            nextHeartRegenTime = nextRegen,
            league = "Bronce",
            avatarData = user.avatarData
        )


        return ResponseEntity.ok(response)
    }

    // ... (Resto de UserController) ...
    @PostMapping("/me/avatar")
    fun updateAvatar(
        @AuthenticationPrincipal userId: String,
        @RequestBody request: AvatarUpdateRequest
    ): ResponseEntity<String> {
        val uuid = UUID.fromString(userId)
        val user = userRepository.findById(uuid).orElseThrow { RuntimeException("User not found") }

        user.avatarData = request.avatarData
        userRepository.save(user)

        return ResponseEntity.ok("Avatar actualizado")
    }

    @GetMapping("/leaderboard/global")
    fun getGlobalLeaderboard(): ResponseEntity<List<LeaderboardEntryDTO>> {
        val topUsers = userRepository.findTop5ByOrderByXpTotalDesc()

        val leaderboard = topUsers.mapIndexed { index, user ->
            LeaderboardEntryDTO(
                userId = user.id!!,
                fullName = user.fullName,
                xpTotal = (user.xpTotal ?: 0L).toInt(),   // 👈 cast a Int
                position = index + 1
            )
        }

        return ResponseEntity.ok(leaderboard)
    }
    @PostMapping("/me/subtract-heart")
    fun subtractHeart(): ResponseEntity<UserProfileResponse> {
        val authentication = SecurityContextHolder.getContext().authentication

        // El .name contiene el UUID (d8413be0...) según tus logs
        val userIdString = authentication.name
        val userId = UUID.fromString(userIdString)

        // 1. Buscamos al usuario usando la nueva función
        val user = userService.getUserById(userId)
            ?: throw RuntimeException("Usuario no encontrado con ID: $userId")

        // 2. Restamos la vida en la base de datos
        val userWithLessHearts = userService.subtractHeart(user.id!!)

        // 3. Calculamos si recuperó vidas por tiempo
        val updatedUser = userService.refreshUserHearts(userWithLessHearts)

        // 4. Devolvemos la respuesta al Frontend
        val response = UserProfileResponse(
            fullName = updatedUser.fullName,
            username = updatedUser.email,
            joinedAt = updatedUser.createdAt,
            totalXp = (updatedUser.xpTotal ?: 0L).toInt(),   // 👈 Int
            currentStreak = (updatedUser.currentStreak ?: 0).toInt(),
            lingots = (updatedUser.lingotsCount ?: 0).toInt(),
            heartsCount = updatedUser.heartsCount,
            nextHeartRegenTime = updatedUser.lastHeartRefillTime?.plusSeconds(300),
            league = "Bronce",
            avatarData = null
        )


        return ResponseEntity.ok(response)
    }

    @GetMapping("/me/challenges")
    fun getMyChallenges(@AuthenticationPrincipal userId: String): ResponseEntity<ChallengesResponse> {
        val uuid = UUID.fromString(userId)
        val user = userRepository.findById(uuid).orElseThrow { RuntimeException("User not found") }

        // Fecha de última práctica
        val lastLessonDate = user.lastPracticeDate
            ?.atZone(java.time.ZoneId.systemDefault())
            ?.toLocalDate()
        val today = java.time.LocalDate.now()
        val isToday = lastLessonDate == today

        // 🔥 Por ahora: valores simples para probar
        // Puedes luego conectarlos a lógica real.
        // Ejemplo: si el usuario practicó hoy, asumimos 20 XP, 10 min, 1 lección perfecta, 2 lecciones completadas.
        val dailyXp: Long = if (isToday) 20L else 0L
        val minutes: Int = if (isToday) 10 else 0
        val perfectLessons: Int = if (isToday) 1 else 0

        // 👉 Aquí puedes usar tu repositorio de progreso de lecciones para algo real
        // Pero para que funcione SIN 500, ponemos 0 o fijo:
        val lessonsToday: Int = if (isToday) 2 else 0

        val xpGoal = 50
        val minutesGoal = 15
        val perfectGoal = 2
        val lessonsGoal = 2

        // 🆕 Reto SQL diario (título y snippet desde la BD), envuelto en try-catch
        val (sqlTitle, sqlSnippet) = try {
            userService.getDailySqlChallenge(uuid)
        } catch (ex: Exception) {
            ex.printStackTrace()
            "Reto extra de inglés" to "Realiza una lección hoy 🎯"
        }

        // 🧩 Construimos los retos para el front
        val xpChallenge = SingleChallengeDTO(
            id = 1,
            type = "XP",
            title = "Gana $xpGoal XP hoy",
            progress = dailyXp.toInt(),
            total = xpGoal
        )

        val timeChallenge = SingleChallengeDTO(
            id = 2,
            type = "TIME",
            title = "Aprende $minutesGoal minutos hoy",
            progress = minutes,
            total = minutesGoal
        )

        val perfectChallenge = SingleChallengeDTO(
            id = 3,
            type = "PERFECT",
            title = "Haz $perfectGoal lecciones perfectas",
            progress = perfectLessons,
            total = perfectGoal
        )

        val lessonsChallenge = SingleChallengeDTO(
            id = 4,
            type = "LESSONS",
            title = "Completa $lessonsGoal lecciones hoy",
            progress = lessonsToday,
            total = lessonsGoal
        )

        // Reto SQL como challenge extra (solo 0/1)
        val sqlChallenge = SingleChallengeDTO(
            id = 5,
            type = "SQL",
            title = sqlTitle,
            progress = if (isToday) 1 else 0,
            total = 1
        )

        val list = listOf(xpChallenge, timeChallenge, perfectChallenge, lessonsChallenge, sqlChallenge)

        // Contamos los completados
        val completed = list.count { it.progress >= it.total }

        return ResponseEntity.ok(
            ChallengesResponse(
                dailyExpProgress = dailyXp,
                dailyExpGoal = xpGoal,
                minutesLearned = minutes,
                minutesGoal = minutesGoal,
                perfectLessonsCount = perfectLessons,
                perfectLessonsGoal = perfectGoal,
                challengesCompleted = completed,
                sqlTitle = sqlTitle,
                sqlSnippet = sqlSnippet,
                challenges = list
            )
        )
    }

    @GetMapping("/search")
    fun search(@RequestParam query: String, @AuthenticationPrincipal userId: String) =
        userService.searchUsers(query, UUID.fromString(userId))

    @PostMapping("/{followedId}/follow")
    fun follow(@PathVariable followedId: UUID, @AuthenticationPrincipal userId: String) =
        userService.followUser(UUID.fromString(userId), followedId)

    @GetMapping("/friends")
    fun getFriends(@AuthenticationPrincipal userId: String) =
        userService.getFollowedUsers(UUID.fromString(userId))

    // 1. Ver solicitudes pendientes que me han llegado a MÍ
    @GetMapping("/friend-requests/pending")
    fun getPendingRequests(@AuthenticationPrincipal userId: String): ResponseEntity<List<StudentDataDTO>> {
        val uuid = UUID.fromString(userId)
        // Necesitarás crear esta función en el userService
        return ResponseEntity.ok(userService.getPendingRequests(uuid))
    }

    // 2. ACEPTAR una solicitud de amistad
    @PostMapping("/friends/accept/{senderId}")
    fun acceptFriend(
        @PathVariable senderId: UUID,
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<String> {
        val myId = UUID.fromString(userId)
        userService.acceptFriendRequest(senderId, myId)
        return ResponseEntity.ok("Ahora son amigos")
    }

    // 3. VER EL PROGRESO DE UN AMIGO (Para ver su perfil al hacer clic)
    @GetMapping("/friends/{friendId}/progress")
    fun getFriendProgress(
        @PathVariable friendId: UUID,
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<DetailedStudentProgressDTO> { // <--- CAMBIA ESTO
        // Ahora 'progress' es un objeto DetailedStudentProgressDTO, no una lista
        val progress = userService.getDetailedProgressForStudent(friendId)
        return ResponseEntity.ok(progress)
    }

    @GetMapping("/admin/all")
    fun getAllUsersAdmin(): ResponseEntity<List<AdminUserDTO>> {
        val users = userRepository.findAll().map { user ->
            AdminUserDTO(
                id = user.id!!,
                fullName = user.fullName,
                email = user.email,
                username = user.email.substringBefore("@"),
                cedula = user.cedula,
                role = user.role.name,
                xpTotal = (user.xpTotal ?: 0L).toInt(),          // 👈 cast a Int
                currentStreak = (user.currentStreak ?: 0).toInt(),
                isActive = user.isActive
            )
        }
        return ResponseEntity.ok(users)
    }
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: UUID,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UpdateUserResponse> {

        val user = userRepository.findById(id)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        user.fullName = request.fullName
        user.email = request.email
        user.cedula = request.cedula

        userRepository.save(user)

        // 👇 AHORA sí devolvemos JSON: { "message": "..." }
        return ResponseEntity.ok(
            UpdateUserResponse(message = "Usuario actualizado correctamente")
        )
    }
    @PutMapping("/admin/status/{id}")
    fun updateUserStatus(
        @PathVariable id: UUID,
        @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<UpdateUserResponse> {

        val user = userRepository.findById(id)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        user.isActive = request.active
        userRepository.save(user)

        return ResponseEntity.ok(
            UpdateUserResponse(message = "Estado actualizado correctamente")
        )
    }

    @PatchMapping("/admin/role/{id}")
    fun updateUserRole(
        @PathVariable id: UUID,
        @RequestBody request: UpdateRoleRequest
    ): ResponseEntity<UpdateUserResponse> {

        val user = userRepository.findById(id)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        user.role = Role.valueOf(request.role.uppercase())

        userRepository.save(user)

        return ResponseEntity.ok(
            UpdateUserResponse("Rol actualizado correctamente")
        )
    }


}