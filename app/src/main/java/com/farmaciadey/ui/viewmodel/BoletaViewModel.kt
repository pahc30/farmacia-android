package com.farmaciadey.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmaciadey.data.repository.PagoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BoletaViewModel(
    private val pagoRepository: PagoRepository = PagoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoletaUiState())
    val uiState: StateFlow<BoletaUiState> = _uiState.asStateFlow()

    fun cargarDatosTransaccion(transaccionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Simular datos de transacción por ahora
            val datosTransaccion = TransaccionData(
                transaccionId = transaccionId,
                monto = 25.50,
                metodoPago = "Visa",
                fecha = "04/11/2024 03:30",
                estado = "Completado"
            )
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                transaccionData = datosTransaccion
            )
        }
    }
}

data class BoletaUiState(
    val isLoading: Boolean = false,
    val transaccionData: TransaccionData? = null,
    val error: String? = null
)

data class TransaccionData(
    val transaccionId: Long,
    val monto: Double,
    val metodoPago: String,
    val fecha: String,
    val estado: String
)