package com.farmaciadey.data.api

import com.farmaciadey.data.models.MetodoPago
import com.farmaciadey.data.models.DataResponse
import retrofit2.Response
import retrofit2.http.POST

interface MetodoPagoApiService {
    
    @POST("metodopago/api/metodopago/list")
    suspend fun getMetodosPago(): Response<DataResponse<List<MetodoPago>>>
    
}
