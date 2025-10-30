package com.farmaciadey.data.repository

import com.farmaciadey.data.models.ItemCarrito
import com.farmaciadey.data.models.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CarritoRepository {
    
    private val _items = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val items: StateFlow<List<ItemCarrito>> = _items.asStateFlow()
    
    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total.asStateFlow()
    
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
    
    fun getCantidadItems(): Int {
        return _items.value.sumOf { it.cantidad }
    }
    
    suspend fun getCartItemCount(): Int {
        return _items.value.size
    }
    
    private fun actualizarTotal() {
        _total.value = _items.value.sumOf { it.subtotal }
    }
}
