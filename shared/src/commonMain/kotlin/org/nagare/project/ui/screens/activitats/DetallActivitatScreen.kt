package org.nagare.project.ui.screens.activitats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.nagare.project.Strings
import org.nagare.project.data.model.Rol
import org.nagare.project.data.model.TipusActivitat
import org.nagare.project.data.model.Usuari

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallActivitatScreen(
    activitatId: String,
    usuari: Usuari,
    navController: NavController,
    vm: DetallActivitatViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsState()
    val esAdmin = usuari.rol == Rol.ADMIN.name

    LaunchedEffect(activitatId) {
        vm.carrega(activitatId, esAdmin)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state is DetallActivitatUiState.Success) (state as DetallActivitatUiState.Success).activitat.titol else "Activitat") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            is DetallActivitatUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is DetallActivitatUiState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(s.missatge, color = MaterialTheme.colorScheme.error)
            }
            is DetallActivitatUiState.Success -> {
                val activitat = s.activitat
                val estaApuntat = activitat.inscrits.contains(usuari.uid)
                val tipus = runCatching { TipusActivitat.valueOf(activitat.tipus) }.getOrDefault(TipusActivitat.COMPETICIO)

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        TipusActivitatChip(tipus)
                        Spacer(Modifier.height(8.dp))
                        Text(activitat.titol, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${formatData(activitat.dataInici)}${activitat.dataFi?.let { " — ${formatData(it)}" } ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (activitat.lloc.isNotBlank()) {
                            Text(activitat.lloc, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (activitat.descripcio.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(activitat.descripcio, style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("${activitat.inscrits.size} ${Strings.INSCRITS.lowercase()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (estaApuntat) vm.desapuntar(usuari.uid, activitatId)
                                else vm.apuntar(usuari.uid, activitatId)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (estaApuntat) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (estaApuntat) Strings.EMDESAPUNTO else Strings.MAPUNTO)
                        }
                    }

                    if (esAdmin && s.inscrits.isNotEmpty()) {
                        item {
                            HorizontalDivider()
                            Spacer(Modifier.height(4.dp))
                            Text(Strings.INSCRITS, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        items(s.inscrits) { inscrit ->
                            InscritRow(inscrit)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InscritRow(usuari: Usuari) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("${usuari.nom} ${usuari.cognoms}", style = MaterialTheme.typography.bodyMedium)
            Text("DNI: ${usuari.dni}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Naix.: ${usuari.dataNaixement}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
