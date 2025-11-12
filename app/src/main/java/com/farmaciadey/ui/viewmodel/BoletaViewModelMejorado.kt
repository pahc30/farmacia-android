package com.farmaciadey.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmaciadey.data.repository.PagoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BoletaMejoradaUiState(
    val isLoading: Boolean = false,
    val boletaPdf: ByteArray? = null,
    val error: String? = null
)

class BoletaViewModelMejorado : ViewModel() {

    private val repository = PagoRepository()
    
    private val _uiState = MutableStateFlow(BoletaMejoradaUiState())
    val uiState: StateFlow<BoletaMejoradaUiState> = _uiState

    fun descargarBoletaTransaccion(transaccionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = repository.descargarBoletaTransaccion(transaccionId)
            
            result.onSuccess { pdfBytes ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    boletaPdf = pdfBytes
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Error al descargar boleta"
                )
            }
        }
    }

    fun descargarBoletaCompra(compraId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = repository.descargarBoletaCompra(compraId)
            
            result.onSuccess { pdfBytes ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    boletaPdf = pdfBytes
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Error al descargar boleta"
                )
            }
        }
    }

    fun limpiarBoleta() {
        _uiState.value = _uiState.value.copy(boletaPdf = null)
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
