package com.mediqueue.app.data

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val id: Long,
    val name: String,
    val email: String,
    val role: String      // "PATIENT" / "DOCTOR" / "ADMIN"
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String
)

// Register er response e token nai
data class RegisterResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: String
)