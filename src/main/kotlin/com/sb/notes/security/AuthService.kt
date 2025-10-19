package com.sb.notes.security

import com.sb.notes.database.model.RefreshToken
import com.sb.notes.database.model.User
import com.sb.notes.database.repository.RefreshTokenRepository
import com.sb.notes.database.repository.UsersRepository
import org.bson.types.ObjectId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.io.encoding.Base64

@Service
class AuthService(
    private val jwtService: JwtService,
    private val usersRepository: UsersRepository,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    fun register(email: String, password: String): User {
        return usersRepository.save(
            User(
                email = email,
                hashedPassword = hashEncoder.encode(password)
            )
        )
    }

    fun login(email: String, password: String): TokenPair {
        val user = usersRepository.findByEmail(email)?: throw BadCredentialsException("Invalid credentials")
        if(!hashEncoder.matches(password, user.hashedPassword)){
            throw BadCredentialsException("Invalid credentials")
        }
        val subject = user.id.toHexString()
        val newAccessToken = jwtService.generateAccessToken(subject)
        val newRefreshToken = jwtService.generateRefreshToken(subject)

        storeRefreshToken(user.id, newRefreshToken)

        return TokenPair(
            newAccessToken,
            newRefreshToken
        )
    }

    @Transactional
    fun refresh(refreshToken: String): TokenPair {
        if(!jwtService.validateRefreshToken(refreshToken)){
            throw BadCredentialsException("Invalid refresh token")
        }

        val userId = jwtService.getUserIdFromJWT(refreshToken)
        val user = usersRepository.findById(ObjectId(userId)).orElseThrow { BadCredentialsException("Invalid refresh token") }

        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndHashedToken(user.id, hashed)
            ?: throw BadCredentialsException("Invalid refresh token")
        refreshTokenRepository.deleteByUserIdAndHashedToken(user.id, hashed)

        val newRefreshToken = jwtService.generateRefreshToken(userId)
        val newAccessToken = jwtService.generateAccessToken(userId)
        storeRefreshToken(user.id, newRefreshToken)
        return TokenPair(
            newAccessToken,
            newRefreshToken
        )
    }

    private fun storeRefreshToken(userId: ObjectId, rawRefreshToken: String) {
        val hashedToken = hashToken(rawRefreshToken)
        val expiresIn = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiresIn)

        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashedToken
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return java.util.Base64.getEncoder().encodeToString(hashBytes)
    }
}