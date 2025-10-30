package com.farmaciadey.data.api

import com.farmaciadey.data.models.DataResponse
import com.farmaciadey.data.models.Producto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ProductoApiService {
    
    @POST("producto/api/producto/list")
    suspend fun getProductos(): Response<DataResponse<List<Producto>>>
    
    @GET("producto/{id}")
    suspend fun getProducto(@Path("id") id: Long): Response<Producto>
    
    @POST("producto/api/producto/search")
    @Headers("Content-Type: text/plain")
    suspend fun searchProductos(@Body searchTerm: String): Response<DataResponse<List<Producto>>>
    
}