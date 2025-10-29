package com.farmaciadey.data.api

import com.farmaciadey.data.models.Compra
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface CompraApiService {
    
    @POST("compra/crear")
    suspend fun crearCompra(
        @Header("Authorization") token: String,
        @Body compra: Compra
    ): Response<Compra>
    
    @GET("compra/usuario/{usuarioId}")
    suspend fun getComprasUsuario(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Long
    ): Response<List<Compra>>
    
}