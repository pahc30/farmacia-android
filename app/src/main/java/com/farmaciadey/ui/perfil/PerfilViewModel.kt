package com.farmaciadey.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.data.api.ApiClient
import com.farmaciadey.data.models.UpdateUsuarioRequest
import com.farmaciadey.data.models.Usuario
import com.farmaciadey.data.repository.AuthRepository
import com.farmaciadey.utils.PreferencesManager
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
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
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
                val isLoggedIn = authRepository.isLoggedIn()
                if (!isLoggedIn) {
                    _uiState.value = PerfilState(error = "Debe iniciar sesión para acceder al perfil")
                    return@launch
                }
                
                val currentUser = preferencesManager.getUser()
                if (currentUser == null) {
                    _uiState.value = PerfilState(error = "No se encontró información del usuario")
                    return@launch
                }
                
                val response = usuarioApiService.getUsuario(currentUser.id)
                if (response.isSuccessful && response.body()?.dato != null) {
                    val updatedUser = response.body()!!.dato!!
                    preferencesManager.saveUser(updatedUser)
                    _uiState.value = PerfilState(usuario = updatedUser)
                } else {
                    _uiState.value = PerfilState(usuario = currentUser)
                }
            } catch (e: Exception) {
                try {
                    val localUser = preferencesManager.getUser()
                    if (localUser != null) {
                        _uiState.value = PerfilState(
                            usuario = localUser,
                            error = "Usando datos guardados (sin conexión)"
                        )
                    } else {
                        _uiState.value = PerfilState(error = e.message ?: "Error al cargar perfil")
                    }
                } catch (e2: Exception) {
                    _uiState.value = PerfilState(error = e.message ?: "Error al cargar perfil")
                }
            }
        }
    }

    fun actualizarPerfil(nombres: String, apellidos: String, email: String, telefono: String, direccion: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null, updateSuccess = false)
            
            try {
                val currentUser = preferencesManager.getUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = "No se encontró información del usuario"
                    )
                    return@launch
                }

                val request = UpdateUsuarioRequest(
                    id = currentUser.id,
                    identificacion = currentUser.identificacion,
                    nombres = nombres,
                    apellidos = apellidos,
                    email = email,
                    telefono = telefono,
                    direccion = direccion,
                    rol = currentUser.rol,
                    username = currentUser.username
                )
                
                val response = usuarioApiService.updateUsuario(currentUser.id, request)
                
                if (response.isSuccessful && response.body()?.dato != null) {
                    val updatedUser = response.body()!!.dato!!
                    preferencesManager.saveUser(updatedUser)
                    _uiState.value = PerfilState(
                        usuario = updatedUser,
                        isUpdating = false,
                        updateSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = "Error al actualizar: ${response.code()}"
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
    
    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(error = null, updateSuccess = false)
    }
}

class PerfilViewModelFactory(private val app: FarmaciaApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PerfilViewModel(
                AuthRepository(app.preferencesManager),
                app.preferencesManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
