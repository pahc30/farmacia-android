package com.farmaciadey.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmaciadey.data.repository.MetodoPagoRepository
import com.farmaciadey.data.models.MetodoPago
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MetodoPagoViewModel(
    private val repository: MetodoPagoRepository = MetodoPagoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetodoPagoUiState())
    val uiState: StateFlow<MetodoPagoUiState> = _uiState.asStateFlow()
    private val TAG = "MetodoPagoViewModel"

    init {
        Log.d(TAG, "ViewModel inicializado, cargando métodos de pago")
        cargarMetodosPago()
    }

    fun cargarMetodosPago() {
        Log.d(TAG, "Iniciando carga de métodos de pago")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getMetodosPago().fold(
                onSuccess = { metodos ->
                    Log.d(TAG, "Métodos de pago cargados exitosamente: ${metodos.size}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        metodosPago = metodos,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error al cargar métodos de pago", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar métodos de pago"
                    )
                }
            )
        }
    }

    fun recargar() {
        cargarMetodosPago()
    }
}

data class MetodoPagoUiState(
    val isLoading: Boolean = false,
    val metodosPago: List<MetodoPago> = emptyList(),
    val error: String? = null
)
