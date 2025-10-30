package com.farmaciadey.data.models

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val dato: JwtResponse?,
    val mensaje: String?,
    val estado: Int
)

data class JwtResponse(
    val accessToken: String,
    val user: Usuario
)

data class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val success: Boolean = true
)

// Respuesta estándar del backend
data class DataResponse<T>(
    val dato: T? = null,
    val mensaje: String? = null,
    val estado: Int = 1
)