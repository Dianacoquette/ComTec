package com.diana.ComTec.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.expiration}")
    private var expiration: Long = 86400000

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(username: String, perfilId: Int, esAdmin: Boolean): String {
        return Jwts.builder()
            .subject(username)
            .claim("perfilId", perfilId)
            .claim("esAdmin", esAdmin)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key)
            .compact()
    }

    fun extractUsername(token: String): String {
        return extractClaims(token).subject
    }

    fun extractPerfilId(token: String): Int {
        return extractClaims(token)["perfilId"] as Int
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            extractClaims(token).expiration.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun extractClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}