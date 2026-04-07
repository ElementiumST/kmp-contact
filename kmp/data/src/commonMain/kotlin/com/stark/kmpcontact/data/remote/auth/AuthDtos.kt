package com.stark.kmpcontact.data.remote.auth

data class LoginRequestDto(
    val login: String,
    val password: String,
    val rememberMe: Boolean,
)

data class LoginResponseDto(
    val sessionId: String,
)
