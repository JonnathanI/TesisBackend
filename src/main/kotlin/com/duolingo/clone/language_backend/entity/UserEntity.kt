package com.duolingo.clone.language_backend.entity

import com.duolingo.clone.language_backend.enums.Role
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant
import java.time.LocalDateTime // Necesitas LocalDateTime para joinedAt
import java.util.UUID

@Entity
@Table(name = "app_user")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    val fullName: String,
    val email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String, // <-- Corregido el nombre (Clash)

    @Enumerated(EnumType.STRING)
    val role: Role = Role.STUDENT,

    var xpTotal: Long = 0, // Tipo Long para XP
    var currentStreak: Int = 0,
    var lastLessonDate: LocalDateTime? = null,

    var heartsCount: Int = 3,
    var lingotsCount: Int = 0,
    var hasStreakFreeze: Boolean = false,

    @Column(columnDefinition = "TEXT")
    var avatarData: String? = null,

    // Campo que estabas usando en UserController
    val createdAt: LocalDateTime = LocalDateTime.now(), // Asumo que usas 'createdAt'
    @Column(name = "last_practice_date")
var lastPracticeDate: Instant? = null, // <-- ¡AGREGADO!

    @Column(name = "last_heart_refill_time")
var lastHeartRefillTime: Instant? = null, // <-- ¡AGREGADO!

) : UserDetails {

    // Implementación de UserDetails
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(role.name))
    }

    override fun getPassword(): String {
        return this.passwordHash // Retorna el campo renombrado
    }

    override fun getUsername(): String {
        return this.email
    }

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}