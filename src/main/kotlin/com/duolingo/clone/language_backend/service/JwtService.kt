package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.entity.UserEntity
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

@Service
class JwtService {

    // Secreto de firma obtenido de application.yml (DEBES AGREGARLO ALLÍ)
    @Value("\${application.security.jwt.secret-key}")
    private lateinit var secretKey: String

    @Value("\${application.security.jwt.expiration}")
    private val jwtExpiration: Long = 86400000 // 24 horas en ms

    // --- Métodos de Extracción ---

    fun extractUsername(token: String): String = extractClaim(token, Claims::getSubject)

    // Extracción de la ID del usuario (lo que usamos como subject)
    fun extractUserId(token: String): String = extractClaim(token, Claims::getSubject)

    fun extractClaim(token: String, claimsResolver: (Claims) -> String): String {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts
            .parser()
            .setSigningKey(signingKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    // --- Métodos de Generación y Validación ---

    fun generateToken(user: UserEntity): String {
        // Usamos el ID del usuario como 'subject' del JWT
        val claims: Map<String, Any> = mapOf(
            "role" to user.role.name,
            "fullName" to user.fullName
        )
        return buildToken(claims, user, jwtExpiration)
    }

    fun isTokenValid(token: String, user: UserEntity): Boolean {
        val userIdInToken = extractUserId(token)
        // Compara el ID del token con el ID del usuario cargado (del DB/cache)
        return userIdInToken == user.id.toString() && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractAllClaims(token).expiration
    }

    // --- Métodos de Firma ---

    private fun buildToken(claims: Map<String, Any>, user: UserEntity, expiration: Long): String {
        return Jwts
            .builder()
            .setClaims(claims)
            .setSubject(user.id.toString()) // Sujeto es el ID del usuario
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(signingKey(), io.jsonwebtoken.SignatureAlgorithm.HS256)
            .compact()
    }

    private fun signingKey(): Key {
        val keyBytes = Decoders.BASE64.decode(secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}