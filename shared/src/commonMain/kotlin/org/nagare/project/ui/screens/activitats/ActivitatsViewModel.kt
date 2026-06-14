package org.nagare.project.ui.screens.activitats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.nagare.project.data.model.Activitat
import org.nagare.project.data.repository.ActivitatsRepository
import org.nagare.project.data.repository.AuthRepository

sealed class ActivitatsUiState {
    data object Loading : ActivitatsUiState()
    data class Success(val properes: List<Activitat>, val passades: List<Activitat>) : ActivitatsUiState()
    data class Error(val missatge: String) : ActivitatsUiState()
}

class ActivitatsViewModel(
    private val activitatsRepo: ActivitatsRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivitatsUiState>(ActivitatsUiState.Loading)
    val uiState: StateFlow<ActivitatsUiState> = _uiState

    init { carrega() }

    fun carrega() {
        viewModelScope.launch {
            _uiState.value = ActivitatsUiState.Loading
            try {
                activitatsRepo.getActivitats().collect { llista ->
                    val ara = Clock.System.now().toEpochMilliseconds()
                    val properes = llista.filter { it.dataInici >= ara }
                    val passades = llista.filter { it.dataInici < ara }.reversed()
                    _uiState.value = ActivitatsUiState.Success(properes, passades)
                }
            } catch (e: Exception) {
                _uiState.value = ActivitatsUiState.Error(e.message ?: "Error desconegut")
            }
        }
    }

    fun creaActivitat(titol: String, descripcio: String, tipus: String, dataInici: Long, dataFi: Long?, lloc: String) {
        viewModelScope.launch {
            try {
                activitatsRepo.createActivitat(
                    Activitat(
                        titol = titol.trim(),
                        descripcio = descripcio.trim(),
                        tipus = tipus,
                        dataInici = dataInici,
                        dataFi = dataFi,
                        lloc = lloc.trim()
                    )
                )
            } catch (_: Exception) {}
        }
    }
}
