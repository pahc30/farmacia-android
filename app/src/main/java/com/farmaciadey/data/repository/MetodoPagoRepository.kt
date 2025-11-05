package com.farmaciadey.data.repository

import android.util.Log
import com.farmaciadey.data.api.ApiClient
import com.farmaciadey.data.models.MetodoPago
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MetodoPagoRepository {
    
    private val apiService = ApiClient.metodoPagoService
    private val TAG = "MetodoPagoRepository"
    
    suspend fun getMetodosPago(): Result<List<MetodoPago>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando llamada a API para obtener métodos de pago")
            val response = apiService.getMetodosPago()
            Log.d(TAG, "Respuesta recibida: ${response.code()} - ${response.message()}")
            
            if (response.isSuccessful) {
                val dataResponse = response.body()
                Log.d(TAG, "Body de respuesta: $dataResponse")
                
                if (dataResponse?.estado == 1 && dataResponse.dato != null) {
                    val metodos = dataResponse.dato.filter { it.eliminado == 0 }
                    Log.d(TAG, "Métodos de pago encontrados: ${metodos.size}")
                    Result.success(metodos)
                } else {
                    val error = dataResponse?.mensaje ?: "Error al obtener métodos de pago"
                    Log.e(TAG, "Error en respuesta: $error")
                    Result.failure(Exception(error))
                }
            } else {
                val error = "Error HTTP: ${response.code()} - ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en getMetodosPago", e)
            Result.failure(e)
        }
    }
}
