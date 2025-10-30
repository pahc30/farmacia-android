package com.farmaciadey.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmaciadey.data.models.RegistroRequest
import com.farmaciadey.data.repository.AuthRepository
import com.farmaciadey.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistroViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun registrarUsuario(
        nombres: String,
        apellidos: String,
        identificacion: String,
        email: String,
        telefono: String,
        usuario: String,
        password: String,
        confirmPassword: String
    ) {
        // Validaciones
        if (!validarCampos(nombres, apellidos, identificacion, email, telefono, usuario, password, confirmPassword)) {
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val registroRequest = RegistroRequest(
            nombres = nombres.trim(),
            apellidos = apellidos.trim(),
            identificacion = identificacion.trim(),
            email = email.trim(),
            telefono = telefono.trim(),
            username = usuario.trim(),  // Mapear usuario a username
            password = password,
            rol = "USER",  // Establecer rol por defecto
            estado = 1
        )

        viewModelScope.launch {
            try {
                val response = authRepository.registrarUsuario(registroRequest)
                if (response.estado == 1) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = response.mensaje ?: "Usuario registrado exitosamente"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.mensaje ?: "Error al registrar usuario"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de conexión: ${e.message}"
                )
            }
        }
    }

    private fun validarCampos(
        nombres: String,
        apellidos: String,
        identificacion: String,
        email: String,
        telefono: String,
        usuario: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        when {
            nombres.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "Los nombres son obligatorios")
                return false
            }
            apellidos.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "Los apellidos son obligatorios")
                return false
            }
            identificacion.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "La identificación es obligatoria")
                return false
            }
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "El email es obligatorio")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "El email no es válido")
                return false
            }
            telefono.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "El teléfono es obligatorio")
                return false
            }
            usuario.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "El nombre de usuario es obligatorio")
                return false
            }
            usuario.length < 3 -> {
                _uiState.value = _uiState.value.copy(errorMessage = "El usuario debe tener al menos 3 caracteres")
                return false
            }
            password.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "La contraseña es obligatoria")
                return false
            }
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
                return false
            }
            password != confirmPassword -> {
                _uiState.value = _uiState.value.copy(errorMessage = "Las contraseñas no coinciden")
                return false
            }
            else -> return true
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false, successMessage = null)
    }
}

data class RegistroUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class RegistroViewModelFactory(private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(AuthRepository(preferencesManager)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}