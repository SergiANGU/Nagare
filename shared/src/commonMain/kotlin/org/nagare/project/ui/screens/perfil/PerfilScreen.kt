package org.nagare.project.ui.screens.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.nagare.project.Strings
import org.nagare.project.data.model.Usuari
import org.nagare.project.data.repository.UsuariRepository
import org.nagare.project.ui.screens.auth.validaDni
import org.nagare.project.ui.screens.auth.validaDataNaixement

// ViewModel inline per al perfil
sealed class PerfilUiState {
    data object Idle : PerfilUiState()
    data object Loading : PerfilUiState()
    data object Success : PerfilUiState()
    data class Error(val missatge: String) : PerfilUiState()
}

class PerfilViewModel(private val usuariRepo: UsuariRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Idle)
    val uiState: StateFlow<PerfilUiState> = _uiState

    fun desa(usuari: Usuari, nom: String, cognoms: String, dni: String, dataNaixement: String) {
        if (nom.isBlank() || cognoms.isBlank() || dni.isBlank() || dataNaixement.isBlank()) {
            _uiState.value = PerfilUiState.Error("Omple tots els camps")
            return
        }
        if (!validaDni(dni)) {
            _uiState.value = PerfilUiState.Error("DNI no vàlid")
            return
        }
        if (!validaDataNaixement(dataNaixement)) {
            _uiState.value = PerfilUiState.Error("La data no pot ser futura")
            return
        }
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Loading
            try {
                usuariRepo.updateUsuari(
                    usuari.copy(
                        nom = nom.trim(),
                        cognoms = cognoms.trim(),
                        dni = dni.trim().uppercase(),
                        dataNaixement = dataNaixement.trim()
                    )
                )
                _uiState.value = PerfilUiState.Success
            } catch (e: Exception) {
                _uiState.value = PerfilUiState.Error(e.message ?: "Error desconegut")
            }
        }
    }

    fun resetState() { _uiState.value = PerfilUiState.Idle }
}

@Composable
fun PerfilScreen(
    usuari: Usuari,
    onActualitzat: (Usuari) -> Unit
) {
    val usuariRepo: UsuariRepository = koinInject()
    val vm = remember { PerfilViewModel(usuariRepo) }
    val state by vm.uiState.collectAsState()

    var editant by remember { mutableStateOf(false) }
    var nom by remember(usuari) { mutableStateOf(usuari.nom) }
    var cognoms by remember(usuari) { mutableStateOf(usuari.cognoms) }
    var dni by remember(usuari) { mutableStateOf(usuari.dni) }
    var dataNaixement by remember(usuari) { mutableStateOf(usuari.dataNaixement) }

    LaunchedEffect(state) {
        if (state is PerfilUiState.Success) {
            vm.resetState()
            editant = false
            onActualitzat(usuari.copy(nom = nom.trim(), cognoms = cognoms.trim(), dni = dni.trim().uppercase(), dataNaixement = dataNaixement.trim()))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(Strings.EL_MEU_PERFIL, style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { editant = !editant }) {
                Text(if (editant) "Cancel·la" else Strings.EDITA)
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Rol: ${usuari.rol}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(usuari.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text(Strings.NOM) }, modifier = Modifier.fillMaxWidth(), enabled = editant, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = cognoms, onValueChange = { cognoms = it }, label = { Text(Strings.COGNOMS) }, modifier = Modifier.fillMaxWidth(), enabled = editant, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = dni, onValueChange = { dni = it }, label = { Text(Strings.DNI) }, modifier = Modifier.fillMaxWidth(), enabled = editant, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = dataNaixement, onValueChange = { dataNaixement = it }, label = { Text(Strings.DATA_NAIXEMENT) }, modifier = Modifier.fillMaxWidth(), enabled = editant, singleLine = true)

        if (state is PerfilUiState.Error) {
            Spacer(Modifier.height(8.dp))
            Text((state as PerfilUiState.Error).missatge, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        if (editant) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.desa(usuari, nom, cognoms, dni, dataNaixement) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is PerfilUiState.Loading
            ) {
                if (state is PerfilUiState.Loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(Strings.DESA)
            }
        }
    }
}
