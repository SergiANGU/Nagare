package org.nagare.project.ui.screens.entrenos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.nagare.project.data.model.Entreno
import org.nagare.project.data.repository.AuthRepository
import org.nagare.project.data.repository.EntrenosRepository

sealed class EntrenosUiState {
    data object Loading : EntrenosUiState()
    data class Success(val propers: List<Entreno>, val passats: List<Entreno>) : EntrenosUiState()
    data class Error(val missatge: String) : EntrenosUiState()
}

class EntrenosViewModel(
    private val entrenosRepo: EntrenosRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EntrenosUiState>(EntrenosUiState.Loading)
    val uiState: StateFlow<EntrenosUiState> = _uiState

    init { carrega() }

    fun carrega() {
        viewModelScope.launch {
            _uiState.value = EntrenosUiState.Loading
            try {
                entrenosRepo.getEntrenos().collect { llista ->
                    val ara = Clock.System.now().toEpochMilliseconds()
                    val propers = llista.filter { it.data >= ara }
                    val passats = llista.filter { it.data < ara }.reversed()
                    _uiState.value = EntrenosUiState.Success(propers, passats)
                }
            } catch (e: Exception) {
                _uiState.value = EntrenosUiState.Error(e.message ?: "Error desconegut")
            }
        }
    }

    fun creaEntreno(titol: String, data: Long, lloc: String, notes: String) {
        viewModelScope.launch {
            try {
                entrenosRepo.createEntreno(Entreno(titol = titol.trim(), data = data, lloc = lloc.trim(), notes = notes.trim()))
            } catch (_: Exception) {}
        }
    }

    fun apuntarAssistent(uid: String, entrenoId: String) {
        viewModelScope.launch {
            try { entrenosRepo.apuntarAssistent(uid, entrenoId) } catch (_: Exception) {}
        }
    }

    fun desapuntarAssistent(uid: String, entrenoId: String) {
        viewModelScope.launch {
            try { entrenosRepo.desapuntarAssistent(uid, entrenoId) } catch (_: Exception) {}
        }
    }
}
