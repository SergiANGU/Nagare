package org.nagare.project.ui.screens.activitats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.nagare.project.Strings
import org.nagare.project.data.model.Activitat
import org.nagare.project.data.model.Rol
import org.nagare.project.data.model.TipusActivitat
import org.nagare.project.data.model.Usuari

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitatsScreen(
    usuari: Usuari,
    navController: NavController,
    vm: ActivitatsViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsState()
    var mostrarFormulari by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (usuari.rol == Rol.ADMIN.name) {
                FloatingActionButton(onClick = { mostrarFormulari = true }) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ActivitatsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ActivitatsUiState.Error -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.missatge, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.carrega() }) { Text(Strings.REINTENTAR) }
                }
                is ActivitatsUiState.Success -> {
                    if (s.properes.isEmpty() && s.passades.isEmpty()) {
                        Text(Strings.SENSE_ACTIVITATS, Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (s.properes.isNotEmpty()) {
                                item { SectionHeader(Strings.PROPERES) }
                                items(s.properes, key = { it.id }) { activitat ->
                                    ActivitatCard(activitat) {
                                        navController.navigate("detall_activitat/${activitat.id}")
                                    }
                                }
                            }
                            if (s.passades.isNotEmpty()) {
                                item { SectionHeader(Strings.PASSADES) }
                                items(s.passades, key = { it.id }) { activitat ->
                                    ActivitatCard(activitat) {
                                        navController.navigate("detall_activitat/${activitat.id}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulari) {
        CrearActivitatDialog(
            onDismiss = { mostrarFormulari = false },
            onCrea = { titol, descripcio, tipus, dataInici, dataFi, lloc ->
                vm.creaActivitat(titol, descripcio, tipus, dataInici, dataFi, lloc)
                mostrarFormulari = false
            }
        )
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun ActivitatCard(activitat: Activitat, onClick: () -> Unit) {
    val tipus = runCatching { TipusActivitat.valueOf(activitat.tipus) }.getOrDefault(TipusActivitat.COMPETICIO)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipusActivitatChip(tipus)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${activitat.inscrits.size} inscrits",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(activitat.titol, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${formatData(activitat.dataInici)}${activitat.dataFi?.let { " — ${formatData(it)}" } ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (activitat.lloc.isNotBlank()) {
                Text(activitat.lloc, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun TipusActivitatChip(tipus: TipusActivitat) {
    val (label, color) = when (tipus) {
        TipusActivitat.COMPETICIO -> "Competició" to MaterialTheme.colorScheme.errorContainer
        TipusActivitat.VIATGE -> "Viatge" to MaterialTheme.colorScheme.tertiaryContainer
        TipusActivitat.ENTRENO_FEDERATIU -> "Fed." to MaterialTheme.colorScheme.primaryContainer
    }
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
    }
}

fun formatData(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    val ld = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth.toString().padStart(2, '0')}/${ld.monthNumber.toString().padStart(2, '0')}/${ld.year}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearActivitatDialog(
    onDismiss: () -> Unit,
    onCrea: (String, String, String, Long, Long?, String) -> Unit
) {
    var titol by remember { mutableStateOf("") }
    var descripcio by remember { mutableStateOf("") }
    var tipus by remember { mutableStateOf(TipusActivitat.COMPETICIO) }
    var expandedTipus by remember { mutableStateOf(false) }
    var dataIniciText by remember { mutableStateOf("") }
    var dataFiText by remember { mutableStateOf("") }
    var lloc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.NOVA_ACTIVITAT) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(value = titol, onValueChange = { titol = it }, label = { Text(Strings.TITOL) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = descripcio, onValueChange = { descripcio = it }, label = { Text(Strings.DESCRIPCIO) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                ExposedDropdownMenuBox(expanded = expandedTipus, onExpandedChange = { expandedTipus = it }) {
                    OutlinedTextField(
                        value = tipus.name, onValueChange = {}, readOnly = true,
                        label = { Text(Strings.TIPUS) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipus) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expandedTipus, onDismissRequest = { expandedTipus = false }) {
                        TipusActivitat.entries.forEach { t ->
                            DropdownMenuItem(text = { Text(t.name) }, onClick = { tipus = t; expandedTipus = false })
                        }
                    }
                }
                OutlinedTextField(value = dataIniciText, onValueChange = { dataIniciText = it }, label = { Text(Strings.DATA_INICI) }, modifier = Modifier.fillMaxWidth(), singleLine = true, supportingText = { Text("dd/MM/yyyy") })
                OutlinedTextField(value = dataFiText, onValueChange = { dataFiText = it }, label = { Text(Strings.DATA_FI) }, modifier = Modifier.fillMaxWidth(), singleLine = true, supportingText = { Text("dd/MM/yyyy") })
                OutlinedTextField(value = lloc, onValueChange = { lloc = it }, label = { Text(Strings.LLOC) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dataInici = parseData(dataIniciText)
                    val dataFi = if (dataFiText.isNotBlank()) parseData(dataFiText) else null
                    if (titol.isNotBlank() && dataInici != null) {
                        onCrea(titol, descripcio, tipus.name, dataInici, dataFi, lloc)
                    }
                },
                enabled = titol.isNotBlank() && dataIniciText.isNotBlank()
            ) { Text(Strings.CREA) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel·la") } }
    )
}

fun parseData(text: String): Long? {
    return try {
        val parts = text.split('/')
        val ldt = LocalDateTime(parts[2].toInt(), parts[1].toInt(), parts[0].toInt(), 0, 0)
        ldt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    } catch (e: Exception) { null }
}
