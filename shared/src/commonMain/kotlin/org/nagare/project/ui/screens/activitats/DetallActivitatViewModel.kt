package org.nagare.project.ui.screens.activitats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.nagare.project.data.model.Activitat
import org.nagare.project.data.model.Usuari
import org.nagare.project.data.repository.ActivitatsRepository
import org.nagare.project.data.repository.UsuariRepository

sealed class DetallActivitatUiState {
    data object Loading : DetallActivitatUiState()
    data class Success(val activitat: Activitat, val inscrits: List<Usuari> = emptyList()) : DetallActivitatUiState()
    data class Error(val missatge: String) : DetallActivitatUiState()
}

class DetallActivitatViewModel(
    private val activitatsRepo: ActivitatsRepository,
    private val usuariRepo: UsuariRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetallActivitatUiState>(DetallActivitatUiState.Loading)
    val uiState: StateFlow<DetallActivitatUiState> = _uiState

    fun carrega(activitatId: String, esAdmin: Boolean) {
        viewModelScope.launch {
            _uiState.value = DetallActivitatUiState.Loading
            try {
                val activitat = activitatsRepo.getActivitat(activitatId)
                    ?: run { _uiState.value = DetallActivitatUiState.Error("Activitat no trobada"); return@launch }
                val inscrits = if (esAdmin) {
                    activitat.inscrits.mapNotNull { uid ->
                        try { usuariRepo.getUsuari(uid) } catch (e: Exception) { null }
                    }
                } else emptyList()
                _uiState.value = DetallActivitatUiState.Success(activitat, inscrits)
            } catch (e: Exception) {
                _uiState.value = DetallActivitatUiState.Error(e.message ?: "Error desconegut")
            }
        }
    }

    fun apuntar(uid: String, activitatId: String, categoria: String? = null) {
        viewModelScope.launch {
            try {
                activitatsRepo.apuntar(uid, activitatId, categoria)
                val current = _uiState.value
                if (current is DetallActivitatUiState.Success) {
                    val novesInscripcions = if (categoria != null) {
                        current.activitat.inscripcions + (uid to categoria)
                    } else {
                        current.activitat.inscripcions
                    }
                    _uiState.value = current.copy(
                        activitat = current.activitat.copy(
                            inscrits = current.activitat.inscrits + uid,
                            inscripcions = novesInscripcions
                        )
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun desapuntar(uid: String, activitatId: String) {
        viewModelScope.launch {
            try {
                activitatsRepo.desapuntar(uid, activitatId)
                val current = _uiState.value
                if (current is DetallActivitatUiState.Success) {
                    val nousList = current.activitat.inscrits - uid
                    _uiState.value = current.copy(activitat = current.activitat.copy(inscrits = nousList))
                }
            } catch (_: Exception) {}
        }
    }
}
