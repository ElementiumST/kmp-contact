package com.stark.kmpcontact.web.contacts.platform

data class LoginRequestDto(
    val login: String,
    val password: String,
    val rememberMe: Boolean,
)

data class LoginResponseDto(
    val sessionId: String,
)
