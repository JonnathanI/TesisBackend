package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.dto.StudentDataDTO
import com.duolingo.clone.language_backend.entity.UserEntity
import com.duolingo.clone.language_backend.enums.Role // <--- Importación correcta
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    // Función para crear un usuario de cualquier rol
    fun createNewUser(email: String, password: String, fullName: String, role: Role): UserEntity {

        require(userRepository.findByEmail(email) == null) { "El email ya está registrado." }

        val passwordEncoded = passwordEncoder.encode(password)

        val newUser = UserEntity(
            email = email.lowercase(),
            passwordHash = passwordEncoded, // Usamos el campo renombrado
            fullName = fullName,
            role = role,
            xpTotal = 0,
            heartsCount = 3,
            currentStreak = 0,
            lingotsCount = 100,
            hasStreakFreeze = false
            // El resto de campos de UserEntity tienen valores por defecto o son nulos
        )
        return userRepository.save(newUser)
    }

    fun registerStudent(email: String, password: String, fullName: String): UserEntity {
        return createNewUser(email, password, fullName, Role.STUDENT)
    }

    fun createAdminUser(email: String, password: String, fullName: String): UserEntity {
        return createNewUser(email, password, fullName, Role.ADMIN)
    }

    fun getUserByEmail(email: String): UserEntity? {
        return userRepository.findByEmail(email)
    }

    fun verifyCredentials(email: String, password: String): Boolean {
        val user = userRepository.findByEmail(email)
        // Usamos el campo renombrado
        return user != null && passwordEncoder.matches(password, user.passwordHash)
    }

    fun getAllStudents(): List<StudentDataDTO> {
        // Usamos Role
        val students = userRepository.findByRole(Role.STUDENT)

        // Asumo que StudentDataDTO usa Long para xpTotal (si no, ajusta el DTO)
        return students.map { user ->
            StudentDataDTO(
                id = user.id!!,
                fullName = user.fullName,
                xpTotal = user.xpTotal,
                email = user.email,
                currentStreak = user.currentStreak
            )
        }
    }
}