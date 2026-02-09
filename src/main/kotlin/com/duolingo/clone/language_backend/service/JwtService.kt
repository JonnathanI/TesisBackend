package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.entity.UserEntity
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey   // 👈 IMPORTANTE: usar SecretKey

@Service
class JwtService {

    @Value("\${application.security.jwt.secret-key}")
    private lateinit var secretKey: String

    @Value("\${application.security.jwt.expiration}")
    private val jwtExpiration: Long = 86400000 // 24 horas

    // ============ EXTRACCIÓN DE CLAIMS ============

    fun extractUsername(token: String): String =
        extractClaim(token) { it.subject }

    fun extractUserId(token: String): String =
        extractClaim(token) { it.subject }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts
            .parser()                      // ✅ API nueva 0.12.x
            .verifyWith(signingKey())      // ✅ ahora devuelve SecretKey
            .build()
            .parseSignedClaims(token)      // ✅ reemplaza a parseClaimsJws
            .payload                       // ✅ en vez de body
    }

    // ============ GENERACIÓN / VALIDACIÓN ============

    fun generateToken(user: UserEntity): String {
        val claims: Map<String, Any> = mapOf(
            "role" to user.role.name,
            "fullName" to user.fullName
        )
        return buildToken(claims, user, jwtExpiration)
    }

    fun isTokenValid(token: String, user: UserEntity): Boolean {
        val userIdInToken = extractUserId(token)
        return userIdInToken == user.id.toString() && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean =
        extractExpiration(token).before(Date())

    private fun extractExpiration(token: String): Date =
        extractAllClaims(token).expiration

    // ============ FIRMA DEL TOKEN ============

    private fun buildToken(
        claims: Map<String, Any>,
        user: UserEntity,
        expiration: Long
    ): String {
        val now = System.currentTimeMillis()

        return Jwts
            .builder()
            .claims(claims)
            .subject(user.id.toString())
            .issuedAt(Date(now))
            .expiration(Date(now + expiration))
            .signWith(signingKey())    // ✅ SecretKey, infiere HS256
            .compact()
    }

    private fun signingKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(secretKey)
        return Keys.hmacShaKeyFor(keyBytes)   // ✅ esto ya es SecretKey
    }
}
