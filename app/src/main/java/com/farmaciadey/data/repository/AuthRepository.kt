package com.farmaciadey.data.repository

import com.farmaciadey.data.api.ApiClient
import com.farmaciadey.data.models.LoginRequest
import com.farmaciadey.data.models.LoginResponse
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
                // Guardar token y usuario
                preferencesManager.saveToken(loginResponse.token)
                preferencesManager.saveUser(loginResponse.user)
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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