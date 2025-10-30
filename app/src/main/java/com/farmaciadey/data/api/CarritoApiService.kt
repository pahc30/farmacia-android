package com.farmaciadey.data.api

import com.farmaciadey.data.models.DataResponse
import com.farmaciadey.data.models.requests.CarritoRequest
import com.farmaciadey.data.models.responses.CarritoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface CarritoApiService {
    
    @POST("compra/api/carrito/save")
    suspend fun agregarAlCarrito(@Body carritoRequest: CarritoRequest): Response<DataResponse<Any>>
    
    @POST("compra/api/carrito/list/{usuarioId}")
    suspend fun getCarritoUsuario(@Path("usuarioId") usuarioId: Int): Response<DataResponse<List<CarritoResponse>>>
    
    @POST("compra/api/carrito/delete/{id}")
    suspend fun eliminarDelCarrito(@Path("id") carritoId: Int): Response<DataResponse<Boolean>>
    
}