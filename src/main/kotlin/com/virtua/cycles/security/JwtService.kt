package com.virtua.cycles.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.Date

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expirationMs: Long
) {

    // Clave secreta constante (en Base64 o UTF-8) para firmar y validar el token
    private val secretKey: Key = Keys.hmacShaKeyFor(secret.toByteArray())

    // Extrae el username (subject) del token JWT
    fun extractUsername(token: String): String? =
        extractClaim(token) { claims -> claims.subject }

    // Extrae cualquier claim del token según una función lambda
    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T? {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    // Genera un token con la información del usuario y expiración
    fun generateToken(userDetails: UserDetails): String {
        val now = Date()
        return Jwts.builder()
            .setSubject(userDetails.username)
            .claim("role", userDetails.authorities.first().authority)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + expirationMs))
            .signWith(secretKey)
            .compact()
    }

    // Valida que el token sea válido para el usuario y no esté expirado
    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    // Verifica si el token expiró
    private fun isTokenExpired(token: String): Boolean {
        val expiration = extractClaim(token) { claims -> claims.expiration }
        return expiration?.before(Date()) ?: true
    }

    // Extrae todos los claims del token
    private fun extractAllClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
}
