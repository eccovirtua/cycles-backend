package com.virtua.cycles.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.Date


@Service
class JwtService {

    // Clave secreta para firmar el token (debe ser larga y segura; aquí un ejemplo para desarrollo)
    private val SECRET_KEY: Key = Keys.secretKeyFor(SignatureAlgorithm.HS256)

    // Extrae el username (subject) del token JWT
    fun extractUsername(token: String): String? {
        return extractClaim(token, Claims::getSubject)
    }

    // Extrae cualquier claim del token según una función lambda
    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T? {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }


    // Genera un token con la información del usuario y expiración (ejemplo 24 horas)
    fun generateToken(userDetails: UserDetails): String {
        val claims = mutableMapOf<String, Any>()

        // Extraer el rol del usuario
        val role = userDetails.authorities.firstOrNull()?.authority ?: "ROLE_USER"
        claims["role"] = role

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10h
            .signWith(SECRET_KEY)  // <- aquí el cambio
            .compact()
    }



    // Valida que el token sea válido para el usuario y no esté expirado
    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return (username == userDetails.username) && !isTokenExpired(token)
    }

    // Verifica si el token expiró
    private fun isTokenExpired(token: String): Boolean {
        val expiration = extractClaim(token, Claims::getExpiration)
        return expiration?.before(Date()) ?: true
    }
    // Extrae todos los claims del token
    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .body
    }
}
