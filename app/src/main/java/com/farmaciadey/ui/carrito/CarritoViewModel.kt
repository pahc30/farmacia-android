package com.farmaciadey.ui.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.data.models.ItemCarrito
import com.farmaciadey.data.repository.CarritoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CarritoState(
    val isLoading: Boolean = false,
    val items: List<ItemCarrito> = emptyList(),
    val total: Double = 0.0,
    val totalItems: Int = 0,
    val error: String? = null
)

class CarritoViewModel(
    private val carritoRepository: CarritoRepository
) : ViewModel() {
    
    private val _carritoState = MutableStateFlow(CarritoState())
    val carritoState: StateFlow<CarritoState> = _carritoState.asStateFlow()
    
    init {
        println("DEBUG: CarritoViewModel.init called")
        observeCarrito()
        cargarCarrito()
    }
    
    private fun observeCarrito() {
        println("DEBUG: CarritoViewModel.observeCarrito called")
        viewModelScope.launch {
            carritoRepository.items.collect { items ->
                println("DEBUG: Repository items changed - size: ${items.size}")
                items.forEach { item ->
                    println("DEBUG: Item: ${item.producto.nombre} - cantidad: ${item.cantidad}")
                }
                _carritoState.value = _carritoState.value.copy(
                    items = items,
                    totalItems = items.sumOf { it.cantidad },
                    total = items.sumOf { it.subtotal }
                )
                println("DEBUG: CarritoState updated - totalItems: ${_carritoState.value.totalItems}, total: ${_carritoState.value.total}")
            }
        }
    }
    
    fun cargarCarrito() {
        println("DEBUG: CarritoViewModel.cargarCarrito called")
        viewModelScope.launch {
            _carritoState.value = _carritoState.value.copy(isLoading = true, error = null)
            
            try {
                carritoRepository.cargarCarrito()
                _carritoState.value = _carritoState.value.copy(isLoading = false)
                println("DEBUG: CarritoViewModel.cargarCarrito completed successfully")
            } catch (e: Exception) {
                println("DEBUG: CarritoViewModel.cargarCarrito error: ${e.message}")
                _carritoState.value = _carritoState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el carrito: ${e.message}"
                )
            }
        }
    }
    
    fun actualizarCantidad(productoId: Int, nuevaCantidad: Int) {
        viewModelScope.launch {
            try {
                carritoRepository.actualizarCantidad(productoId, nuevaCantidad)
            } catch (e: Exception) {
                _carritoState.value = _carritoState.value.copy(
                    error = "Error al actualizar cantidad: ${e.message}"
                )
            }
        }
    }
    
    fun eliminarProducto(productoId: Int) {
        viewModelScope.launch {
            try {
                carritoRepository.quitarProducto(productoId)
            } catch (e: Exception) {
                _carritoState.value = _carritoState.value.copy(
                    error = "Error al eliminar producto: ${e.message}"
                )
            }
        }
    }
    
    fun limpiarError() {
        _carritoState.value = _carritoState.value.copy(error = null)
    }
}

class CarritoViewModelFactory(private val app: FarmaciaApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarritoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarritoViewModel(app.carritoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
