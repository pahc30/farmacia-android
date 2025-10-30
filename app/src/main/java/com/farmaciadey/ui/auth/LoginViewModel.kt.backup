package com.farmaciadey.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmaciadey.data.repository.AuthRepository
import com.farmaciadey.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)
            
            authRepository.login(username, password)
                .onSuccess {
                    _loginState.value = LoginState(isSuccess = true)
                }
                .onFailure { exception ->
                    _loginState.value = LoginState(error = exception.message ?: "Error de conexión")
                }
        }
    }
}

class LoginViewModelFactory(private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(AuthRepository(preferencesManager)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}