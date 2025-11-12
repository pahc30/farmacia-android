package com.farmaciadey.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farmaciadey.data.repository.CompraRepository
import com.farmaciadey.data.models.CompraBackend
import com.farmaciadey.data.models.DetalleCompraBackend
import com.farmaciadey.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistorialComprasViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HistorialComprasUiState())
    val uiState: StateFlow<HistorialComprasUiState> = _uiState.asStateFlow()
    
    private val preferencesManager = PreferencesManager(application)
    private val compraRepository = CompraRepository(preferencesManager)

    fun cargarHistorialCompras() {
        viewModelScope.launch {
            Log.d("HistorialViewModel", "Iniciando carga de historial...")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Obtener el ID del usuario logueado
            val currentUser = preferencesManager.getUser()
            if (currentUser == null) {
                Log.e("HistorialViewModel", "Usuario no logueado")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Debe iniciar sesión para ver el historial"
                )
                return@launch
            }
            
            Log.d("HistorialViewModel", "Usuario ID: ${currentUser.id}")
            
            compraRepository.getHistorialCompras(currentUser.id).fold(
                onSuccess = { comprasBackend ->
                    Log.d("HistorialViewModel", "Compras recibidas del backend: ${comprasBackend.size}")
                    
                    val comprasUI = comprasBackend.map { compraBackend ->
                        Log.d("HistorialViewModel", "Mapeando compra ID: ${compraBackend.id}, MetodoPago: ${compraBackend.metodoPago}, Total: ${compraBackend.total}")
                        CompraHistorial(
                            transaccionId = compraBackend.id?.toLong() ?: 0L,
                            fecha = formatearFecha(compraBackend.fecha),
                            monto = compraBackend.total ?: 0.0,
                            metodoPago = compraBackend.metodoPago ?: "Desconocido",
                            estado = "Completado",
                            productos = obtenerNombresProductos(compraBackend.detalleCompra)
                        )
                    }
                    
                    Log.d("HistorialViewModel", "Compras UI mapeadas: ${comprasUI.size}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        compras = comprasUI
                    )
                    
                    Log.d("HistorialViewModel", "Estado actualizado, compras en UI: ${_uiState.value.compras.size}")
                },
                onFailure = { exception ->
                    Log.e("HistorialViewModel", "Error al cargar historial: ${exception.message}", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar historial"
                    )
                }
            )
        }
    }
    
    private fun formatearFecha(fecha: Date?): String {
        return if (fecha != null) {
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            // La fecha viene en UTC del backend, la mostramos en hora de Perú (UTC-5)
            formatter.timeZone = TimeZone.getTimeZone("America/Lima")
            formatter.format(fecha)
        } else {
            "Fecha no disponible"
        }
    }
    
    private fun obtenerNombresProductos(detalles: List<DetalleCompraBackend>?): String {
        return detalles?.mapNotNull { detalle ->
            detalle.producto?.nombre
        }?.joinToString(", ") ?: "Productos no disponibles"
    }
}

data class HistorialComprasUiState(
    val isLoading: Boolean = false,
    val compras: List<CompraHistorial> = emptyList(),
    val error: String? = null
)

data class CompraHistorial(
    val transaccionId: Long,
    val fecha: String,
    val monto: Double,
    val metodoPago: String,
    val estado: String,
    val productos: String
)