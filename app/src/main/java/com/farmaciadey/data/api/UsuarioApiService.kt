package com.farmaciadey.data.api

import com.farmaciadey.data.models.Usuario
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface UsuarioApiService {
    
    @GET("usuario/perfil")
    suspend fun getPerfil(@Header("Authorization") token: String): Response<Usuario>
    
}