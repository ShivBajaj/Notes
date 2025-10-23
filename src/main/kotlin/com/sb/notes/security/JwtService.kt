package com.sb.notes.security

import com.sb.notes.database.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.Base64
import java.util.Date
import javax.crypto.spec.SecretKeySpec

@Service
class JwtService(
    @Value("\${jwt.secret}") val jwtSecret: String,
) {
    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))
    private val accessTokenValidityMs = 15L * 60L * 1000L
    val refreshTokenValidityMs = 30L * 24 * 60 * 60 * 1000L

    private fun generateToken(
        userId: String,
        type: String,
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userId: String): String {
        return generateToken(userId, "access", accessTokenValidityMs)
    }

    fun generateRefreshToken(userId: String): String {
        return generateToken(userId, "refresh", refreshTokenValidityMs)
    }

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "access"
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "refresh"
    }

    fun getUserIdFromToken(token: String): String {
        val claims = parseAllClaims(token) ?: throw ResponseStatusException(
            HttpStatusCode.valueOf(401),
            "Invalid token."
        )
        return claims.subject
    }

    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if(token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else token
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch(e: Exception) {
            null
        }
    }
//    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))
//
//    private val accessTokenValidityMs: Long = 15L * 60L * 1000L
//    val refreshTokenValidityMs: Long = 30L * 24L * 60L * 60L * 1000L
//
//
//    fun generateAccessToken(userId: String): String {
//        return generateToken(userId, "access_token", accessTokenValidityMs)
//    }
//
//    fun generateRefreshToken(userId: String): String {
//        return generateToken(userId, "refresh_token", refreshTokenValidityMs)
//    }
//
//    fun getUserIdFromJWT(token: String): String {
//        val claims = parseAllClaims(token) ?: throw IllegalArgumentException("Invalid JWT token")
//        return claims.subject
//    }
//
//    fun validateAccessToken(accessToken: String): Boolean {
//        val claims = parseAllClaims(accessToken) ?: return false
//        val tokenType = claims["type"] as? String ?: return false
//        return tokenType == "access_token"
//    }
//
//    fun validateRefreshToken(refreshToken: String): Boolean {
//        val claims = parseAllClaims(refreshToken) ?: return false
//        val tokenType = claims["type"] as? String ?: return false
//        return tokenType == "refresh_token"
//    }
//
//    private fun generateToken(
//        userId: String,
//        type: String,
//        expiresIn: Long
//    ): String {
//        val now = Date()
//        val expirationDate = Date(now.time + expiresIn)
//
//        return Jwts.builder()
//            .subject(userId)
//            .claim("type", type)
//            .issuedAt(now)
//            .expiration(expirationDate)
//            .signWith(secretKey, Jwts.SIG.HS256)
//            .compact()
//    }
//
//    private fun parseAllClaims(token: String): Claims? {
//
//        val rawToken = if(token.startsWith("Bearer ")) {
//            token.removePrefix("Bearer ")
//        }else token
//
//        return try {
//            Jwts.parser()
//                .verifyWith(secretKey)
//                .build()
//                .parseSignedClaims(rawToken)
//                .payload
//        }catch (e : Exception){
//            null
//        }
//    }
}