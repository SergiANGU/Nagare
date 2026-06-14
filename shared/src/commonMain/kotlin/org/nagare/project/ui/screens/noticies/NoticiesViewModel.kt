package org.nagare.project.ui.screens.noticies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.nagare.project.data.model.Noticia
import org.nagare.project.data.repository.AuthRepository
import org.nagare.project.data.repository.NoticiesRepository

sealed class NoticiesUiState {
    data object Loading : NoticiesUiState()
    data class Success(val noticies: List<Noticia>) : NoticiesUiState()
    data class Error(val missatge: String) : NoticiesUiState()
}

class NoticiesViewModel(
    private val noticiesRepo: NoticiesRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoticiesUiState>(NoticiesUiState.Loading)
    val uiState: StateFlow<NoticiesUiState> = _uiState

    init {
        carrega()
    }

    fun carrega() {
        viewModelScope.launch {
            _uiState.value = NoticiesUiState.Loading
            try {
                noticiesRepo.getNoticies().collect { llista ->
                    _uiState.value = NoticiesUiState.Success(llista)
                }
            } catch (e: Exception) {
                _uiState.value = NoticiesUiState.Error(e.message ?: "Error desconegut")
            }
        }
    }

    fun publicaNoticia(titol: String, cos: String, tipus: String) {
        val uid = authRepo.currentUid() ?: return
        viewModelScope.launch {
            try {
                val noticia = Noticia(
                    titol = titol.trim(),
                    cos = cos.trim(),
                    tipus = tipus,
                    data = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                    autorUid = uid
                )
                noticiesRepo.createNoticia(noticia)
            } catch (_: Exception) {}
        }
    }
}
