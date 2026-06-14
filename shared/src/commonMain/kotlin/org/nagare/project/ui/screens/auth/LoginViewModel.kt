package org.nagare.project.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.nagare.project.data.repository.AuthRepository
import org.nagare.project.data.repository.UsuariRepository

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val missatge: String) : LoginUiState()
}

class LoginViewModel(
    private val authRepo: AuthRepository,
    private val usuariRepo: UsuariRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Omple tots els camps")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                authRepo.signIn(email, password)
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Credencials incorrectes. Torna-ho a intentar.")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
