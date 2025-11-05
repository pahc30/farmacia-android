package com.farmaciadey.data.api

import com.farmaciadey.data.models.DataResponse
import com.farmaciadey.data.models.CompraBackend
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface CompraApiService {
    
    @POST("compra/api/compra/save")
    suspend fun crearCompra(@Body compra: Any): Response<DataResponse<Any>>
    
    @POST("compra/api/compra/list/{usuarioId}")
    suspend fun getComprasUsuario(@Path("usuarioId") usuarioId: Int): Response<DataResponse<List<CompraBackend>>>
}
