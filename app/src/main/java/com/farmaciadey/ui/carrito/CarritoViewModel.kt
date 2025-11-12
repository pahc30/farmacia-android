package com.farmaciadey.ui.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.data.models.ItemCarrito
import com.farmaciadey.data.repository.CarritoRepository
import com.farmaciadey.data.repository.CompraRepository
import com.farmaciadey.utils.PreferencesManager
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
    private val carritoRepository: CarritoRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _carritoState = MutableStateFlow(CarritoState())
    val carritoState: StateFlow<CarritoState> = _carritoState.asStateFlow()
    
    init {
        println("DEBUG: CarritoViewModel.init called")
        cargarCarrito()
    }
    
    fun cargarCarrito() {
        viewModelScope.launch {
            try {
                _carritoState.value = _carritoState.value.copy(isLoading = true, error = null)
                println("DEBUG: Collecting carrito items from repository")
                carritoRepository.items.collect { items ->
                    val total = carritoRepository.total.value
                    actualizarCarrito(items, total)
                }
            } catch (e: Exception) {
                println("ERROR: CarritoViewModel.cargarCarrito - ${e.message}")
                e.printStackTrace()
                _carritoState.value = _carritoState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el carrito: ${e.message}"
                )
            }
        }
    }
    
    fun agregarProducto(productoId: Int, cantidad: Int = 1) {
        // Este método no se usa actualmente, se agrega desde ProductoDetailFragment
    }
    
    fun actualizarCantidad(productoId: Int, cantidad: Int) {
        viewModelScope.launch {
            try {
                carritoRepository.actualizarCantidad(productoId, cantidad)
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
    
    suspend fun crearCompraDesdeCarrito(metodoPagoId: Int = 1): Result<Int> {
        return try {
            // IMPORTANTE: Obtener items frescos del repositorio, no del estado cacheado
            val itemsFrescos = carritoRepository.items.value
            val totalFresco = carritoRepository.total.value
            
            if (itemsFrescos.isEmpty()) {
                return Result.failure(Exception("El carrito está vacío"))
            }
            
            // Obtener usuario actual
            val usuario = preferencesManager.getUser()
                ?: return Result.failure(Exception("Usuario no autenticado"))
            
            // Preparar detalles de la compra
            val detalleCompra = itemsFrescos.map { item ->
                mapOf(
                    "productoId" to item.producto.id,
                    "cantidad" to item.cantidad,
                    "precio" to item.producto.precio,
                    "subtotal" to item.subtotal
                )
            }
            
            // Preparar request de compra
            val compraRequest = mapOf(
                "usuarioId" to usuario.id,
                "metodoPagoId" to metodoPagoId,
                "subtotal" to totalFresco,
                "total" to totalFresco,
                "detalleCompra" to detalleCompra
            )
            
            // Crear compra en el backend usando CompraRepository
            val compraRepository = CompraRepository(preferencesManager)
            compraRepository.crearCompra(compraRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun actualizarCarrito(items: List<ItemCarrito>, total: Double) {
        val totalItems = items.sumOf { it.cantidad }
        
        _carritoState.value = _carritoState.value.copy(
            isLoading = false,
            items = items,
            total = total,
            totalItems = totalItems,
            error = null
        )
        
        println("DEBUG: CarritoViewModel updated - Total: $total, Items: $totalItems")
    }
    
    fun limpiarCarritoLocal() {
        // Vaciar el carrito local inmediatamente (el backend ya lo vació)
        _carritoState.value = CarritoState(
            isLoading = false,
            items = emptyList(),
            total = 0.0,
            totalItems = 0,
            error = null
        )
        println("DEBUG: Carrito local vaciado después de crear compra")
    }
    
    fun limpiarError() {
        _carritoState.value = _carritoState.value.copy(error = null)
    }
}

class CarritoViewModelFactory(private val app: FarmaciaApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarritoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarritoViewModel(app.carritoRepository, app.preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
