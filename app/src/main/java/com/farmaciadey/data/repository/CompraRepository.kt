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
}
