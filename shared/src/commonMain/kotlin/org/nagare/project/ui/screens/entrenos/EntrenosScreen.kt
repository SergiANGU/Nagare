package org.nagare.project.ui.screens.entrenos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.nagare.project.Strings
import org.nagare.project.data.model.Entreno
import org.nagare.project.data.model.Rol
import org.nagare.project.data.model.Usuari
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.nagare.project.ui.screens.activitats.SectionHeader
import org.nagare.project.ui.screens.activitats.formatData

@Composable
fun EntrenosScreen(
    usuari: Usuari,
    vm: EntrenosViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsState()
    val esAdmin = usuari.rol == Rol.ADMIN.name
    var mostrarFormulari by remember { mutableStateOf(false) }
    var entrenoExpandit by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            if (esAdmin) {
                FloatingActionButton(onClick = { mostrarFormulari = true }) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is EntrenosUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is EntrenosUiState.Error -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.missatge, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.carrega() }) { Text(Strings.REINTENTAR) }
                }
                is EntrenosUiState.Success -> {
                    if (s.propers.isEmpty() && s.passats.isEmpty()) {
                        Text(Strings.SENSE_ENTRENOS, Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (s.propers.isNotEmpty()) {
                                item { SectionHeader(Strings.PROPERS) }
                                items(s.propers, key = { it.id }) { entreno ->
                                    EntrenoCard(
                                        entreno = entreno,
                                        uid = usuari.uid,
                                        esAdmin = esAdmin,
                                        esProp = true,
                                        expandit = entrenoExpandit == entreno.id,
                                        onExpandir = { entrenoExpandit = if (entrenoExpandit == entreno.id) null else entreno.id },
                                        onApuntar = { vm.apuntarAssistent(usuari.uid, entreno.id) },
                                        onDesapuntar = { vm.desapuntarAssistent(usuari.uid, entreno.id) }
                                    )
                                }
                            }
                            if (s.passats.isNotEmpty()) {
                                item { SectionHeader(Strings.PASSATS) }
                                items(s.passats, key = { it.id }) { entreno ->
                                    EntrenoCard(
                                        entreno = entreno,
                                        uid = usuari.uid,
                                        esAdmin = esAdmin,
                                        esProp = false,
                                        expandit = entrenoExpandit == entreno.id,
                                        onExpandir = { entrenoExpandit = if (entrenoExpandit == entreno.id) null else entreno.id },
                                        onApuntar = {},
                                        onDesapuntar = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulari) {
        CrearEntrenoDialog(
            onDismiss = { mostrarFormulari = false },
            onCrea = { titol, data, lloc, notes ->
                vm.creaEntreno(titol, data, lloc, notes)
                mostrarFormulari = false
            }
        )
    }
}

@Composable
fun EntrenoCard(
    entreno: Entreno,
    uid: String,
    esAdmin: Boolean,
    esProp: Boolean,
    expandit: Boolean,
    onExpandir: () -> Unit,
    onApuntar: () -> Unit,
    onDesapuntar: () -> Unit
) {
    val estaApuntat = entreno.assistents.contains(uid)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(entreno.titol, style = MaterialTheme.typography.titleMedium)
            Text(
                "${formatData(entreno.data)}${if (entreno.lloc.isNotBlank()) " · ${entreno.lloc}" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (esProp) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = if (estaApuntat) onDesapuntar else onApuntar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (estaApuntat) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (estaApuntat) Strings.NO_HI_ANIRÉ else Strings.HI_ASSISTIRE)
                    }
                    if (esAdmin) {
                        OutlinedButton(onClick = onExpandir, modifier = Modifier.weight(1f)) {
                            Text("${entreno.assistents.size} ass.")
                        }
                    }
                }
            } else if (esAdmin) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "${entreno.assistents.size} ${Strings.ASSISTENTS.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (esAdmin && expandit && entreno.assistents.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text(Strings.ASSISTENTS, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                entreno.assistents.forEach { assUid ->
                    Text("• $assUid", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun CrearEntrenoDialog(
    onDismiss: () -> Unit,
    onCrea: (String, Long, String, String) -> Unit
) {
    var titol by remember { mutableStateOf("") }
    var dataText by remember { mutableStateOf("") }
    var lloc by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.NOU_ENTRENO) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = titol, onValueChange = { titol = it }, label = { Text(Strings.TITOL) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = dataText, onValueChange = { dataText = it }, label = { Text(Strings.DATA_INICI) }, modifier = Modifier.fillMaxWidth(), singleLine = true, supportingText = { Text("dd/MM/yyyy") })
                OutlinedTextField(value = lloc, onValueChange = { lloc = it }, label = { Text(Strings.LLOC) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(Strings.NOTES) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val data = parseData(dataText)
                    if (titol.isNotBlank() && data != null) onCrea(titol, data, lloc, notes)
                },
                enabled = titol.isNotBlank() && dataText.isNotBlank()
            ) { Text(Strings.CREA) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel·la") } }
    )
}

private fun parseData(text: String): Long? {
    return try {
        val parts = text.split('/')
        val ldt = LocalDateTime(parts[2].toInt(), parts[1].toInt(), parts[0].toInt(), 0, 0)
        ldt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    } catch (e: Exception) { null }
}
