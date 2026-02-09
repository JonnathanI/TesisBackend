package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.*
import com.duolingo.clone.language_backend.enums.Role // <-- ¡IMPORTACIÓN CORREGIDA!
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

// Asumo que tu entidad UserEntity ya usa el enum Role
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?

    // Usamos el tipo Role (que acabamos de importar)
    fun findByRole(role: Role): List<UserEntity>

    fun findTop5ByOrderByXpTotalDesc(): List<UserEntity>
    fun findByCedula(cedula: String): UserEntity?
    // En UserRepository.kt
    // En UserRepository.kt
    @Query("SELECT u FROM UserEntity u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchByNameOrEmail(query: String): List<UserEntity>

    @Query(
        """
        SELECT DISTINCT s
        FROM CourseEntity c
        JOIN c.students s
        WHERE c.teacher.id = :teacherId
    """
    )
    fun findStudentsByTeacherId(@Param("teacherId") teacherId: UUID): List<UserEntity>
}