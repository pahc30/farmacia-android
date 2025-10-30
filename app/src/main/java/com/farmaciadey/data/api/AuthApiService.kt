package com.farmaciadey.data.api

import com.farmaciadey.data.models.LoginRequest
import com.farmaciadey.data.models.LoginResponse
import com.farmaciadey.data.models.RegistroRequest
import com.farmaciadey.data.models.RegistroResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("auth/api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("usuario/api/usuarios/registrar")
    suspend fun registrar(@Body registroRequest: RegistroRequest): Response<RegistroResponse>
    
}