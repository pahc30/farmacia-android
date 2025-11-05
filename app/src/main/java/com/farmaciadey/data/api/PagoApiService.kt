package com.farmaciadey.data.api

import com.farmaciadey.data.models.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface PagoApiService {
    
    @POST("metodopago/api/v1/pagos/crear-intent")
    suspend fun crearPago(@Body request: PaymentIntentRequest): Response<PaymentIntentResponse>
    
    @POST("metodopago/api/v1/pagos/confirmar/{transaccionId}")
    suspend fun confirmarPago(@Path("transaccionId") transaccionId: Long): Response<PagoResponse>
    
    @GET("metodopago/api/v1/pagos/transaccion/{transaccionId}")
    suspend fun obtenerEstadoPago(@Path("transaccionId") transaccionId: Long): Response<TransaccionPago>
    
    @GET("metodopago/api/v1/pagos/compra/{compraId}")
    suspend fun obtenerTransaccionesPorCompra(@Path("compraId") compraId: Long): Response<List<TransaccionPago>>
    
    @GET("metodopago/api/v1/pagos/boleta/transaccion/{transaccionId}")
    suspend fun descargarBoletaPorTransaccion(@Path("transaccionId") transaccionId: Long): Response<ResponseBody>
    
    @GET("metodopago/api/v1/pagos/boleta/compra/{compraId}")
    suspend fun descargarBoletaPorCompra(@Path("compraId") compraId: Long): Response<ResponseBody>
    
    @GET("metodopago/api/v1/pagos/health")
    suspend fun healthCheck(): Response<Map<String, Any>>
    
    @GET("metodopago/api/v1/pagos/info")
    suspend fun obtenerInformacion(): Response<Map<String, Any>>
}
