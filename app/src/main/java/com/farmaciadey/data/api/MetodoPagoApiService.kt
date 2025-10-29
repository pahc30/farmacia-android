package com.farmaciadey.data.api

import com.farmaciadey.data.models.MetodoPago
import retrofit2.Response
import retrofit2.http.GET

interface MetodoPagoApiService {
    
    @GET("metodopago/list")
    suspend fun getMetodosPago(): Response<List<MetodoPago>>
    
}