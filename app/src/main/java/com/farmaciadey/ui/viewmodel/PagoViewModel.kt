package com.farmaciadey.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmaciadey.data.repository.PagoRepository
import com.farmaciadey.data.repository.CarritoRepository
import com.farmaciadey.data.models.CrearPagoRequest
import com.farmaciadey.data.models.PagoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PagoViewModel(
    private val pagoRepository: PagoRepository = PagoRepository(),
    private val carritoRepository: CarritoRepository = CarritoRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    fun crearPago(request: CrearPagoRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            pagoRepository.crearPago(request).fold(
                onSuccess = { pagoResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pagoResponse = pagoResponse,
                        message = "Pago procesado exitosamente"
                    )
                    // Limpiar carrito después del pago exitoso
                    if (pagoResponse.success) {
                        limpiarCarritoDespuesDePago()
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al procesar pago"
                    )
                }
            )
        }
    }

    fun confirmarPago(transaccionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            pagoRepository.confirmarPago(transaccionId).fold(
                onSuccess = { pagoResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pagoResponse = pagoResponse,
                        message = "Pago confirmado exitosamente"
                    )
                    // Limpiar carrito después del pago exitoso
                    limpiarCarritoDespuesDePago()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al confirmar pago"
                    )
                }
            )
        }
    }

    fun procesarPagoConSimulacion(metodoPagoId: Long, monto: Double) {
        // Método de compatibilidad para fragments existentes
        val request = CrearPagoRequest(
            compraId = 1L, // ID temporal de compra
            metodoPagoId = metodoPagoId,
            monto = monto,
            moneda = "PEN",
            descripcion = "Compra de medicamentos"
        )
        crearPago(request)
    }

    fun getMensajeMetodoPago(metodoPagoId: Long): String {
        return when (metodoPagoId) {
            1L, 2L -> "Yape/Plin" 
            3L -> "Visa"
            else -> "Método de pago"
        }
    }

    fun clearState() {
        _uiState.value = PagoUiState()
    }
    
    private fun limpiarCarritoDespuesDePago() {
        viewModelScope.launch {
            // Limpiar carrito local (usuario ID temporal: 1)
            carritoRepository.limpiarCarritoRemoto(12).fold(
                onSuccess = {
                    // Carrito limpiado exitosamente
                },
                onFailure = {
                    // Intentar limpiar solo local si falla remoto
                    carritoRepository.limpiarCarrito()
                }
            )
        }
    }
}

data class PagoUiState(
    val isLoading: Boolean = false,
    val pagoResponse: PagoResponse? = null,
    val message: String? = null,
    val error: String? = null
)