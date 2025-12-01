package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.StudentDataDTO
import com.duolingo.clone.language_backend.entity.UserEntity
import com.duolingo.clone.language_backend.entity.UserRole
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant       // <-- ¡IMPORTA ESTO!
import java.time.LocalDateTime // <-- ¡E IMPORTA ESTO!
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    // Función para crear un usuario de cualquier rol (¡CORREGIDA!)
    fun createNewUser(email: String, password: String, fullName: String, role: UserRole): UserEntity {

        // CORREGIDO: Tu repo devuelve UserEntity?, así que comparamos con null
        require(userRepository.findByEmail(email) == null) { "El email ya está registrado." }

        val passwordHash = passwordEncoder.encode(password)

        val newUser = UserEntity(
            email = email.lowercase(),
            passwordHash = passwordHash,
            fullName = fullName,
            role = role,
            xpTotal = 0,
            heartsCount = if (role == UserRole.STUDENT) 5 else 0,

            // --- ¡CAMPOS CORREGIDOS CON TIPOS MIXTOS! ---
            createdAt = LocalDateTime.now(),      // <-- Este espera LocalDateTime
            lastPracticeDate = Instant.now(),       // <-- Este espera Instant
            lastHeartRefillTime = Instant.now(),    // <-- Este espera Instant
            currentStreak = 0,
            lingotsCount = 100,
            hasStreakFreeze = false,
            isActive = true
        )
        return userRepository.save(newUser)
    }

    // Función específica para registrar ESTUDIANTES
    fun registerStudent(email: String, password: String, fullName: String): UserEntity {
        return createNewUser(email, password, fullName, UserRole.STUDENT)
    }

    // === NUEVA FUNCIÓN PARA ADMINISTRADORES ===
    fun createAdminUser(email: String, password: String, fullName: String): UserEntity {
        return createNewUser(email, password, fullName, UserRole.ADMIN)
    }

    fun getUserByEmail(email: String): UserEntity? {
        // CORREGIDO: No se necesita .orElse(null)
        return userRepository.findByEmail(email)
    }

    fun verifyCredentials(email: String, password: String): Boolean {
        // CORREGIDO: No se necesita .orElse(null)
        val user = userRepository.findByEmail(email)
        return user != null && passwordEncoder.matches(password, user.passwordHash)
    }
    fun getAllStudents(): List<StudentDataDTO> {
        // Usamos el método findByRole que ya tienes en tu UserRepository
        val students = userRepository.findByRole(UserRole.STUDENT)

        // Convertimos la lista de UserEntity a StudentDataDTO
        return students.map { user ->
            StudentDataDTO(
                id = user.id!!, // Asumimos que el ID no es nulo
                fullName = user.fullName,
                xpTotal = user.xpTotal,
                currentStreak = user.currentStreak
            )
        }
    }
}