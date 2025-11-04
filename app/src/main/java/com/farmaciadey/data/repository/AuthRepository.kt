package com.farmaciadey.data.repository

import com.farmaciadey.data.api.ApiClient
import com.farmaciadey.data.models.LoginRequest
import com.farmaciadey.data.models.LoginResponse
import com.farmaciadey.data.models.RegistroRequest
import com.farmaciadey.data.models.RegistroResponse
import com.farmaciadey.data.models.Usuario
import com.farmaciadey.utils.PreferencesManager
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val preferencesManager: PreferencesManager) {
    
    private val authService = ApiClient.authService
    
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = authService.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                if (loginResponse.estado == 1 && loginResponse.dato != null) {
                    // Guardar token y usuario
                    preferencesManager.saveToken(loginResponse.dato.accessToken)
                    preferencesManager.saveUser(loginResponse.dato.user)
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception(loginResponse.mensaje ?: "Credenciales incorrectas"))
                }
            } else {
                Result.failure(Exception("Error de conexión"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loginAutomatico(username: String, password: String): Boolean {
        return try {
            val result = login(username, password)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getToken(): String? {
        return preferencesManager.getTokenAsync()
    }
    
    suspend fun registrarUsuario(registroRequest: RegistroRequest): RegistroResponse {
        val response = authService.registrar(registroRequest)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        } else {
            throw Exception("Error al registrar usuario: ${response.message()}")
        }
    }
    
    suspend fun logout() {
        preferencesManager.clearSession()
    }
    
    suspend fun isLoggedIn(): Boolean {
        return preferencesManager.isLoggedIn()
    }
    
    suspend fun getCurrentUser(): Usuario? {
        return preferencesManager.getUser()
    }
    
    fun getUserFlow(): Flow<Usuario?> {
        return preferencesManager.userFlow
    }
    
    fun getTokenFlow(): Flow<String?> {
        return preferencesManager.tokenFlow
    }
}