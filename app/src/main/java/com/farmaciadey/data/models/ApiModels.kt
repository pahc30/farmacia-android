package com.farmaciadey.data.models

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: Usuario
)

data class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val success: Boolean = true
)