package com.farmaciadey.data.repository

import com.farmaciadey.data.api.ApiClient
import com.farmaciadey.data.models.CompraBackend
import com.farmaciadey.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CompraRepository(private val preferencesManager: PreferencesManager) {
    
    private val compraApiService = ApiClient.compraService

    suspend fun getHistorialCompras(usuarioId: Int): Result<List<CompraBackend>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = compraApiService.getComprasUsuario(usuarioId)
                if (response.isSuccessful && response.body()?.dato != null) {
                    Result.success(response.body()!!.dato!!)
                } else {
                    Result.failure(Exception("Error al cargar historial de compras: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun crearCompra(compraRequest: Map<String, Any>): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val response = compraApiService.crearCompra(compraRequest)
                if (response.isSuccessful && response.body()?.dato != null) {
                    val compraCreada = response.body()!!.dato as? Map<*, *>
                    val compraId = when (val id = compraCreada?.get("id")) {
                        is Number -> id.toInt()
                        is String -> id.toInt()
                        else -> throw Exception("ID de compra no válido")
                    }
                    Result.success(compraId)
                } else {
                    Result.failure(Exception("Error al crear compra: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
