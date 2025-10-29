package com.farmaciadey.data.repository

import com.farmaciadey.data.api.ApiClient
import com.farmaciadey.data.models.Compra
import com.farmaciadey.utils.PreferencesManager

class CompraRepository(private val preferencesManager: PreferencesManager) {
    
    private val compraService = ApiClient.compraService
    
    suspend fun crearCompra(compra: Compra): Result<Compra> {
        return try {
            val token = preferencesManager.getTokenAsync()
            if (token != null) {
                val response = compraService.crearCompra("Bearer $token", compra)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error al procesar la compra"))
                }
            } else {
                Result.failure(Exception("Usuario no autenticado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getComprasUsuario(usuarioId: Long): Result<List<Compra>> {
        return try {
            val token = preferencesManager.getTokenAsync()
            if (token != null) {
                val response = compraService.getComprasUsuario("Bearer $token", usuarioId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error al cargar historial"))
                }
            } else {
                Result.failure(Exception("Usuario no autenticado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}