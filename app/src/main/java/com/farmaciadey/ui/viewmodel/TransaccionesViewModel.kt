package com.farmaciadey.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmaciadey.data.models.TransaccionPago
import com.farmaciadey.data.repository.PagoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el historial de transacciones
 */
class TransaccionesViewModel : ViewModel() {
    
    private val pagoRepository = PagoRepository()
    
    private val _uiState = MutableStateFlow(TransaccionesUiState())
    val uiState: StateFlow<TransaccionesUiState> = _uiState.asStateFlow()
    
    /**
     * Carga las transacciones de una compra específica
     */
    fun cargarTransacciones(compraId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            pagoRepository.obtenerTransaccionesPorCompra(compraId)
                .onSuccess { transacciones ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transacciones = transacciones,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar transacciones"
                    )
                }
        }
    }
    
    /**
     * Actualiza el estado de una transacción específica
     */
    fun actualizarTransaccion(transaccionId: Long) {
        viewModelScope.launch {
            pagoRepository.obtenerEstadoPago(transaccionId)
                .onSuccess { transaccion ->
                    val transaccionesActualizadas = _uiState.value.transacciones.map { t ->
                        if (t.id == transaccionId) transaccion else t
                    }
                    _uiState.value = _uiState.value.copy(
                        transacciones = transaccionesActualizadas
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Error al actualizar transacción"
                    )
                }
        }
    }
    
    /**
     * Limpia los errores
     */
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * Estado UI para el historial de transacciones
 */
data class TransaccionesUiState(
    val isLoading: Boolean = false,
    val transacciones: List<TransaccionPago> = emptyList(),
    val error: String? = null
)
