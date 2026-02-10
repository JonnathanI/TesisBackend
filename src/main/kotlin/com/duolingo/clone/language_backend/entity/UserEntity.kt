package com.duolingo.clone.language_backend.entity

import com.duolingo.clone.language_backend.enums.Role
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "app_user")
@JsonIgnoreProperties(value = ["hibernateLazyInitializer", "handler"])
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    // ⬇⬇ CAMBIADOS A var
    var fullName: String,
    var email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    var role: Role,


    var xpTotal: Long = 0,
    var currentStreak: Int = 0,
    var lastLessonDate: LocalDateTime? = null,

    var heartsCount: Int = 3,
    var lingotsCount: Int = 0,
    var hasStreakFreeze: Boolean = false,

    @Column(columnDefinition = "TEXT")
    var avatarData: String? = null,

    val createdAt: Instant? = null,

    @Column(name = "last_practice_date")
    var lastPracticeDate: Instant? = null,

    @Column(name = "last_heart_refill_time")
    var lastHeartRefillTime: Instant? = null,

    @Column(nullable = false)
    var isActive: Boolean = true,

    var registeredById: UUID?,
    var registeredByName: String,

    @Enumerated(EnumType.STRING)
    var registeredByRole: Role,

    @Column(nullable = false, unique = true, length = 10)
    var cedula: String,
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(role.name))
    }

    override fun getPassword(): String = this.passwordHash

    override fun getUsername(): String = this.email

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = isActive
}
