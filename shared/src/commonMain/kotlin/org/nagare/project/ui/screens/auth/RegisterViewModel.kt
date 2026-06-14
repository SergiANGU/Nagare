package org.nagare.project.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.nagare.project.data.repository.AuthRepository

sealed class RegisterUiState {
    data object Idle : RegisterUiState()
    data object Loading : RegisterUiState()
    data class Success(val uid: String, val email: String) : RegisterUiState()
    data class Error(val missatge: String) : RegisterUiState()
}

class RegisterViewModel(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = RegisterUiState.Error("Omple tots els camps")
            return
        }
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                val user = authRepo.signUp(email, password)
                _uiState.value = RegisterUiState.Success(
                    uid = user.uid,
                    email = user.email ?: email
                )
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(e.message ?: "S'ha produït un error")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
