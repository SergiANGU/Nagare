package org.nagare.project.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.nagare.project.data.model.Rol
import org.nagare.project.data.model.Usuari
import org.nagare.project.data.repository.AuthRepository
import org.nagare.project.data.repository.UsuariRepository

sealed class CompletarPerfilUiState {
    data object Idle : CompletarPerfilUiState()
    data object Loading : CompletarPerfilUiState()
    data object Success : CompletarPerfilUiState()
    data class Error(val missatge: String) : CompletarPerfilUiState()
}

class CompletarPerfilViewModel(
    private val authRepo: AuthRepository,
    private val usuariRepo: UsuariRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CompletarPerfilUiState>(CompletarPerfilUiState.Idle)
    val uiState: StateFlow<CompletarPerfilUiState> = _uiState

    fun desa(nom: String, cognoms: String, dni: String, dataNaixement: String, email: String) {
        if (nom.isBlank() || cognoms.isBlank() || dni.isBlank() || dataNaixement.isBlank()) {
            _uiState.value = CompletarPerfilUiState.Error("Omple tots els camps obligatoris")
            return
        }
        if (!validaDni(dni)) {
            _uiState.value = CompletarPerfilUiState.Error("DNI no vàlid")
            return
        }
        if (!validaDataNaixement(dataNaixement)) {
            _uiState.value = CompletarPerfilUiState.Error("La data no pot ser futura")
            return
        }
        val uid = authRepo.currentUid() ?: run {
            _uiState.value = CompletarPerfilUiState.Error("Sessió no trobada")
            return
        }
        viewModelScope.launch {
            _uiState.value = CompletarPerfilUiState.Loading
            try {
                val usuari = Usuari(
                    uid = uid,
                    nom = nom.trim(),
                    cognoms = cognoms.trim(),
                    dni = dni.trim().uppercase(),
                    dataNaixement = dataNaixement.trim(),
                    email = email.trim(),
                    rol = Rol.MEMBRE.name,
                    creatEl = Clock.System.now().toEpochMilliseconds()
                )
                usuariRepo.createUsuari(usuari)
                _uiState.value = CompletarPerfilUiState.Success
            } catch (e: Exception) {
                _uiState.value = CompletarPerfilUiState.Error(e.message ?: "S'ha produït un error")
            }
        }
    }

    fun resetState() {
        _uiState.value = CompletarPerfilUiState.Idle
    }
}

fun validaDni(dni: String): Boolean {
    val clean = dni.trim().uppercase()
    if (clean.length != 9) return false
    val lletres = "TRWAGMYFPDXBNJZSQVHLCKE"
    val numero = clean.substring(0, 8).toIntOrNull() ?: return false
    val lletra = clean[8]
    return lletra == lletres[numero % 23]
}

fun validaDataNaixement(data: String): Boolean {
    // Format esperat: dd/MM/yyyy o yyyy-MM-dd
    return try {
        val avui = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val parts = if (data.contains('/')) {
            val p = data.split('/')
            kotlinx.datetime.LocalDate(p[2].toInt(), p[1].toInt(), p[0].toInt())
        } else {
            val p = data.split('-')
            kotlinx.datetime.LocalDate(p[0].toInt(), p[1].toInt(), p[2].toInt())
        }
        parts <= avui
    } catch (e: Exception) {
        false
    }
}
