package com.farmaciadey.data.api

import com.farmaciadey.data.models.Producto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductoApiService {
    
    @GET("producto/list")
    suspend fun getProductos(): Response<List<Producto>>
    
    @GET("producto/{id}")
    suspend fun getProducto(@Path("id") id: Long): Response<Producto>
    
}