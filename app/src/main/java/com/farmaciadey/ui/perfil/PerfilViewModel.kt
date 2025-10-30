package com.farmaciadey.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmaciadey.FarmaciaApplication
import com.farmaciadey.data.models.Usuario
import com.farmaciadey.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PerfilState(
    val usuario: Usuario? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PerfilViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _perfilState = MutableStateFlow(PerfilState())
    val perfilState: StateFlow<PerfilState> = _perfilState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _perfilState.value = PerfilState(isLoading = true)
            
            try {
                val usuario = authRepository.getCurrentUser()
                _perfilState.value = PerfilState(usuario = usuario)
            } catch (e: Exception) {
                _perfilState.value = PerfilState(error = e.message ?: "Error al cargar perfil")
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