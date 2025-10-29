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
    
    fun agregarProducto(producto: Producto, cantidad: Int = 1) {
        val currentItems = _items.value.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.producto.id == producto.id }
        
        if (existingItemIndex != -1) {
            // Producto ya existe, actualizar cantidad
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(cantidad = existingItem.cantidad + cantidad)
        } else {
            // Nuevo producto
            currentItems.add(ItemCarrito(producto, cantidad))
        }
        
        _items.value = currentItems
        actualizarTotal()
    }
    
    fun quitarProducto(productoId: Long) {
        val currentItems = _items.value.toMutableList()
        currentItems.removeAll { it.producto.id == productoId }
        _items.value = currentItems
        actualizarTotal()
    }
    
    fun actualizarCantidad(productoId: Long, nuevaCantidad: Int) {
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
    
    fun limpiarCarrito() {
        _items.value = emptyList()
        _total.value = 0.0
    }
    
    fun getCantidadItems(): Int {
        return _items.value.sumOf { it.cantidad }
    }
    
    private fun actualizarTotal() {
        _total.value = _items.value.sumOf { it.subtotal }
    }
}