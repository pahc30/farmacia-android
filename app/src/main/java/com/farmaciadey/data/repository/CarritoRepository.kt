package com.farmaciadey.data.repository

import com.farmaciadey.data.models.ItemCarrito
import com.farmaciadey.data.models.Producto
import com.farmaciadey.data.api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CarritoRepository {
    
    companion object {
        @Volatile
        private var INSTANCE: CarritoRepository? = null
        
        fun getInstance(): CarritoRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CarritoRepository().also { INSTANCE = it }
            }
        }
    }
    
    private val _items = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val items: StateFlow<List<ItemCarrito>> = _items.asStateFlow()
    
    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total.asStateFlow()
    
    private val carritoApiService = ApiClient.createCarritoService()
    
    suspend fun cargarCarrito() {
        // El carrito ahora es solo temporal durante la sesión
        // No se persiste entre sesiones
    }
    
    suspend fun agregarProducto(producto: Producto, cantidad: Int = 1) {
        val currentItems = _items.value.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.producto.id == producto.id }
        
        if (existingItemIndex != -1) {
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(cantidad = existingItem.cantidad + cantidad)
        } else {
            currentItems.add(ItemCarrito(id = null, producto = producto, cantidad = cantidad))
        }
        
        _items.value = currentItems
        actualizarTotal()
    }
    
    suspend fun quitarProducto(productoId: Int) {
        val currentItems = _items.value.toMutableList()
        currentItems.removeAll { it.producto.id == productoId }
        _items.value = currentItems
        actualizarTotal()
    }
    
    suspend fun actualizarCantidad(productoId: Int, nuevaCantidad: Int) {
        if (nuevaCantidad <= 0) {
            quitarProducto(productoId)
            return
        }
        
        val currentItems = _items.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.producto.id == productoId }
        
        if (itemIndex != -1) {
            currentItems[itemIndex] = currentItems[itemIndex].copy(cantidad = nuevaCantidad)
            _items.value = currentItems
            actualizarTotal()
        }
    }
    
    suspend fun limpiarCarrito() {
        _items.value = emptyList()
        _total.value = 0.0
    }
    
    suspend fun limpiarCarritoRemoto(usuarioId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = carritoApiService.limpiarCarrito(usuarioId)
                if (response.isSuccessful) {
                    // También limpiar el carrito local
                    limpiarCarrito()
                    Result.success(true)
                } else {
                    Result.failure(Exception("Error al limpiar carrito: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    fun getCantidadItems(): Int {
        return _items.value.sumOf { it.cantidad }
    }
    
    suspend fun getCartItemCount(): Int {
        return _items.value.size
    }
    
    fun getCarritoItemCount() = _items.map { items -> 
        items.sumOf { it.cantidad }
    }
    
    private fun actualizarTotal() {
        _total.value = _items.value.sumOf { it.subtotal }
    }
}
