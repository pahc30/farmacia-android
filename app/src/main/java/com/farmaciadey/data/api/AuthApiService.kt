package com.farmaciadey.data.api

import com.farmaciadey.data.models.LoginRequest
import com.farmaciadey.data.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
}