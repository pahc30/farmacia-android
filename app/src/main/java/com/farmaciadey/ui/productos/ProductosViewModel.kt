package com.farmaciadey.ui.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.data.models.Producto
import com.farmaciadey.data.repository.CarritoRepository
import com.farmaciadey.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductosState(
    val isLoading: Boolean = false,
    val productos: List<Producto>? = null,
    val error: String? = null
)

class ProductosViewModel(
    private val productoRepository: ProductoRepository,
    private val carritoRepository: CarritoRepository
) : ViewModel() {
    
    private val _productosState = MutableStateFlow(ProductosState())
    val productosState: StateFlow<ProductosState> = _productosState.asStateFlow()
    
    fun loadProductos() {
        viewModelScope.launch {
            _productosState.value = ProductosState(isLoading = true)
            
            productoRepository.getProductos()
                .onSuccess { productos ->
                    _productosState.value = ProductosState(productos = productos)
                }
                .onFailure { exception ->
                    _productosState.value = ProductosState(error = exception.message ?: "Error al cargar productos")
                }
        }
    }
    
    fun agregarAlCarrito(producto: Producto) {
        carritoRepository.agregarProducto(producto)
    }
}

class ProductosViewModelFactory(private val app: FarmaciaApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductosViewModel(
                ProductoRepository(),
                CarritoRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}