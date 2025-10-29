package com.farmaciadey.data.repository

import com.farmaciadey.data.api.ApiClient
import com.farmaciadey.data.models.Producto

class ProductoRepository {
    
    private val productoService = ApiClient.productoService
    
    suspend fun getProductos(): Result<List<Producto>> {
        return try {
            val response = productoService.getProductos()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar productos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProducto(id: Long): Result<Producto> {
        return try {
            val response = productoService.getProducto(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Producto no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}