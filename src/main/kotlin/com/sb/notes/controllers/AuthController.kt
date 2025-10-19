package com.sb.notes.controllers

import com.sb.notes.security.AuthService
import com.sb.notes.security.JwtService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController (
    val authService: AuthService
){
    data class AuthRequest(
        val email: String,
        val password: String
    )

    data class RefreshRequest(
        val refreshToken : String
    )

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @RequestBody request: AuthRequest
    ) {
        authService.register(request.email, request.password)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: AuthRequest
    ): AuthService.TokenPair {
        return authService.login(request.email, request.password)
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody request: RefreshRequest
    ): AuthService.TokenPair {
        return authService.refresh(request.refreshToken)
    }

}