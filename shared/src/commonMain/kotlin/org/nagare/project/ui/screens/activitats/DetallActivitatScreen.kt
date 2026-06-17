package org.nagare.project.ui.screens.activitats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.nagare.project.Strings
import org.nagare.project.data.model.Rol
import org.nagare.project.data.model.TipusActivitat
import org.nagare.project.data.model.Usuari

private fun categoriesPerGenere(genere: String): List<String> = when (genere) {
    "HOME" -> listOf("-62 kg", "-69 kg", "-77 kg", "-85 kg", "-94 kg", "+94 kg")
    "DONA" -> listOf("-55 kg", "-62 kg", "-70 kg", "+70 kg")
    else -> emptyList()
}

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
    var mostrarDialogCategoria by remember { mutableStateOf(false) }

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
                val esCompeticio = tipus == TipusActivitat.COMPETICIO
                val genereSense = usuari.genere.isBlank()

                if (mostrarDialogCategoria) {
                    CategoriaDialog(
                        categories = categoriesPerGenere(usuari.genere),
                        onConfirmar = { categoria ->
                            vm.apuntar(usuari.uid, activitatId, categoria)
                            mostrarDialogCategoria = false
                        },
                        onDismiss = { mostrarDialogCategoria = false }
                    )
                }

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
                        Text(
                            "${activitat.inscrits.size} ${Strings.INSCRITS.lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))

                        when {
                            estaApuntat -> {
                                Button(
                                    onClick = { vm.desapuntar(usuari.uid, activitatId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text(Strings.EMDESAPUNTO)
                                }
                                activitat.inscripcions[usuari.uid]?.let { cat ->
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${Strings.CATEGORIA_PES}: $cat",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            esCompeticio && genereSense -> {
                                Text(
                                    Strings.SENSE_GENERE_COMPETICIO,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            esCompeticio -> {
                                Button(
                                    onClick = { mostrarDialogCategoria = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(Strings.MAPUNTO)
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { vm.apuntar(usuari.uid, activitatId) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(Strings.MAPUNTO)
                                }
                            }
                        }
                    }

                    if (esAdmin && s.inscrits.isNotEmpty()) {
                        item {
                            HorizontalDivider()
                            Spacer(Modifier.height(4.dp))
                            Text(Strings.INSCRITS, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        items(s.inscrits) { inscrit ->
                            InscritRow(inscrit, activitat.inscripcions[inscrit.uid])
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriaDialog(
    categories: List<String>,
    onConfirmar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var seleccionada by remember { mutableStateOf(categories.firstOrNull() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.SELECCIONA_CATEGORIA) },
        text = {
            Column(Modifier.selectableGroup()) {
                categories.forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = seleccionada == cat,
                                onClick = { seleccionada = cat },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = seleccionada == cat,
                            onClick = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(cat, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (seleccionada.isNotBlank()) onConfirmar(seleccionada) }) {
                Text(Strings.CONFIRMAR)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.CANCEL_LA)
            }
        }
    )
}

@Composable
private fun InscritRow(usuari: Usuari, categoria: String?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("${usuari.nom} ${usuari.cognoms}", style = MaterialTheme.typography.bodyMedium)
            Text("DNI: ${usuari.dni}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Naix.: ${usuari.dataNaixement}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!categoria.isNullOrBlank()) {
                Text("${Strings.CATEGORIA_PES}: $categoria", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
