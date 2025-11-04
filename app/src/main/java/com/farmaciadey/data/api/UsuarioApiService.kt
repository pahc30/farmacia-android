package com.farmaciadey.data.api

import com.farmaciadey.data.models.DataResponse
import com.farmaciadey.data.models.UpdateUsuarioRequest
import com.farmaciadey.data.models.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * API Service para operaciones relacionadas con usuarios
 */
interface UsuarioApiService {
    
    /**
     * Obtiene la información de un usuario por su ID
     */
    @POST("usuario/api/usuarios/find/{usuarioId}")
    suspend fun getUsuario(@Path("usuarioId") usuarioId: Int): Response<DataResponse<Usuario>>
    
    /**
     * Actualiza la información de un usuario (sin modificar password)
     */
    @POST("usuario/api/usuarios/update/{usuarioId}")
    suspend fun updateUsuario(
        @Path("usuarioId") usuarioId: Int, 
        @Body request: UpdateUsuarioRequest
    ): Response<DataResponse<Usuario>>
}
