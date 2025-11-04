package com.farmaciadey.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.data.api.ApiClient
import com.farmaciadey.data.models.UpdateUsuarioRequest
import com.farmaciadey.data.models.Usuario
import com.farmaciadey.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PerfilState(
    val usuario: Usuario? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

class PerfilViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PerfilState())
    val uiState: StateFlow<PerfilState> = _uiState.asStateFlow()
    
    private val usuarioApiService = ApiClient.usuarioService
    
    init {
        cargarPerfil()
    }
    
    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.value = PerfilState(isLoading = true)
            
            try {
                // Verificar si el usuario está autenticado
                val isLoggedIn = authRepository.isLoggedIn()
                if (!isLoggedIn) {
                    _uiState.value = PerfilState(error = "Debe iniciar sesión para acceder al perfil")
                    return@launch
                }
                
                // Usar usuario Test (ID 12) para consistencia con el historial
                val response = usuarioApiService.getUsuario(12)
                if (response.isSuccessful && response.body()?.dato != null) {
                    _uiState.value = PerfilState(usuario = response.body()!!.dato)
                } else {
                    _uiState.value = PerfilState(error = "Error al cargar perfil: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = PerfilState(error = e.message ?: "Error al cargar perfil")
            }
        }
    }
    
    fun actualizarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null, updateSuccess = false)
            
            try {
                val updateRequest = UpdateUsuarioRequest(
                    id = usuario.id,
                    identificacion = usuario.identificacion,
                    nombres = usuario.nombres,
                    apellidos = usuario.apellidos,
                    telefono = usuario.telefono,
                    email = usuario.email,
                    direccion = usuario.direccion,
                    rol = usuario.rol,
                    username = usuario.username
                )
                val response = usuarioApiService.updateUsuario(usuario.id, updateRequest)
                if (response.isSuccessful && response.body()?.dato != null) {
                    _uiState.value = PerfilState(
                        usuario = response.body()!!.dato,
                        updateSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = "Error al actualizar perfil: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = e.message ?: "Error al actualizar perfil"
                )
            }
        }
    }
    
    fun cerrarSesion() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

class PerfilViewModelFactory(private val app: FarmaciaApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PerfilViewModel(
                AuthRepository(app.preferencesManager)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
