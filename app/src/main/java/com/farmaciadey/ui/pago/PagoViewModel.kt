package com.farmaciadey.ui.pago

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmaciadey.data.models.*
import com.farmaciadey.data.repository.PagoRepository
import kotlinx.coroutines.launch

class PagoViewModel : ViewModel() {
    
    private val pagoRepository = PagoRepository()
    
    // LiveData para el estado del pago
    private val _estadoPago = MutableLiveData<String>()
    val estadoPago: LiveData<String> = _estadoPago
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _pagoResult = MutableLiveData<PagoResponse?>()
    val pagoResult: LiveData<PagoResponse?> = _pagoResult
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _metodosPago = MutableLiveData<List<MetodoPago>>()
    val metodosPago: LiveData<List<MetodoPago>> = _metodosPago
    
    private val _metodoSeleccionado = MutableLiveData<MetodoPago?>()
    val metodoSeleccionado: LiveData<MetodoPago?> = _metodoSeleccionado
    
    init {
        cargarMetodosPago()
    }
    
    private fun cargarMetodosPago() {
        viewModelScope.launch {
            try {
                // Simular métodos de pago disponibles (normalmente vendrían del backend)
                val metodos = listOf(
                    MetodoPago(id = 2, descripcion = "Billeteras Digitales", tipo = "Yape/Plin"),
                    MetodoPago(id = 3, descripcion = "Pagos con Tarjetas Debito/Credito", tipo = "Visa")
                )
                _metodosPago.value = metodos.filter { it.isActive }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar métodos de pago: ${e.message}"
            }
        }
    }
    
    fun seleccionarMetodoPago(metodo: MetodoPago) {
        _metodoSeleccionado.value = metodo
        _errorMessage.value = null
    }
    
    fun procesarPago(compraId: Long, monto: Double, descripcion: String = "Compra en Farmacia DeY") {
        val metodo = _metodoSeleccionado.value
        if (metodo == null) {
            _errorMessage.value = "Debe seleccionar un método de pago"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _pagoResult.value = null
                _errorMessage.value = null
                
                // Crear request de pago
                
                // Procesar pago con simulación de estados
                val resultado = pagoRepository.procesarPagoConSimulacion(
                    metodoPago = metodo,
                    monto = monto
                ) { estado ->
                    _estadoPago.postValue(estado)
                }
                
                resultado.fold(
                    onSuccess = { response ->
                        _pagoResult.value = response
                        if (response.success) {
                            _estadoPago.value = "✅ Pago completado exitosamente"
                        } else {
                            _estadoPago.value = "❌ ${response.message}"
                            _errorMessage.value = response.message
                        }
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Error al procesar pago: ${exception.message}"
                        _estadoPago.value = "❌ Error en el procesamiento"
                    }
                )
                
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _estadoPago.value = "❌ Error inesperado"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getMensajeMetodoPago(): String {
        return _metodoSeleccionado.value?.let { metodo ->
            pagoRepository.getMensajeMetodoPago(metodo.tipo)
        } ?: "Seleccione un método de pago"
    }
    
    fun procesarPagoYapePlin(codigo: String, monto: Double, descripcion: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _pagoResult.value = null
                _errorMessage.value = null
                
                // Simular estados intermedios
                _estadoPago.value = "Verificando código..."
                kotlinx.coroutines.delay(1000)
                
                _estadoPago.value = "Conectando con Yape/Plin..."
                kotlinx.coroutines.delay(1500)
                
                _estadoPago.value = "Procesando pago..."
                kotlinx.coroutines.delay(2000)
                
                // Crear request para Yape/Plin
                val request = CrearPagoRequest(
                    compraId = System.currentTimeMillis(),
                    metodoPagoId = 2L, // Yape/Plin
                    monto = monto,
                    descripcion = descripcion,
                    codigoPago = codigo
                )
                
                val resultado = pagoRepository.crearPago(request)
                
                resultado.fold(
                    onSuccess = { response ->
                        _pagoResult.value = response
                        if (response.success) {
                            _estadoPago.value = "✅ Pago completado exitosamente"
                        } else {
                            _estadoPago.value = "❌ ${response.message}"
                            _errorMessage.value = response.message
                        }
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Error al procesar pago: ${exception.message}"
                        _estadoPago.value = "❌ Error en el procesamiento"
                    }
                )
                
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _estadoPago.value = "❌ Error inesperado"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun procesarPagoVisa(
        numeroTarjeta: String,
        fechaExpiracion: String,
        cvv: String,
        nombreTitular: String,
        monto: Double,
        descripcion: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _pagoResult.value = null
                _errorMessage.value = null
                
                // Simular estados intermedios
                _estadoPago.value = "Validando tarjeta..."
                kotlinx.coroutines.delay(1000)
                
                _estadoPago.value = "Conectando con banco..."
                kotlinx.coroutines.delay(1500)
                
                _estadoPago.value = "Autorizando pago..."
                kotlinx.coroutines.delay(2000)
                
                _estadoPago.value = "Procesando transacción..."
                kotlinx.coroutines.delay(1500)
                
                // Crear request para Visa
                val request = CrearPagoRequest(
                    compraId = System.currentTimeMillis(),
                    metodoPagoId = 2L, // Visa
                    monto = monto,
                    descripcion = descripcion,
                    datosTarjeta = DatosTarjeta(
                        numeroTarjeta = numeroTarjeta,
                        fechaExpiracion = fechaExpiracion,
                        cvv = cvv,
                        nombreTitular = nombreTitular
                    )
                )
                
                val resultado = pagoRepository.crearPago(request)
                
                resultado.fold(
                    onSuccess = { response ->
                        _pagoResult.value = response
                        if (response.success) {
                            _estadoPago.value = "✅ Pago completado exitosamente"
                        } else {
                            _estadoPago.value = "❌ ${response.message}"
                            _errorMessage.value = response.message
                        }
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Error al procesar pago: ${exception.message}"
                        _estadoPago.value = "❌ Error en el procesamiento"
                    }
                )
                
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _estadoPago.value = "❌ Error inesperado"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun limpiarEstado() {
        _pagoResult.value = null
        _errorMessage.value = null
        _estadoPago.value = ""
        _isLoading.value = false
    }
    
    fun reintentar() {
        _errorMessage.value = null
        _pagoResult.value = null
    }
}
