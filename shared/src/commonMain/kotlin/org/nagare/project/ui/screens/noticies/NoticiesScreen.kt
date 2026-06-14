package org.nagare.project.ui.screens.noticies

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import org.koin.compose.viewmodel.koinViewModel
import org.nagare.project.Strings
import org.nagare.project.data.model.Noticia
import org.nagare.project.data.model.Rol
import org.nagare.project.data.model.TipusNoticia
import org.nagare.project.data.model.Usuari

@Composable
fun NoticiesScreen(
    usuari: Usuari,
    vm: NoticiesViewModel = koinViewModel()
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                is NoticiesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NoticiesUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(s.missatge, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.carrega() }) { Text(Strings.REINTENTAR) }
                    }
                }
                is NoticiesUiState.Success -> {
                    if (s.noticies.isEmpty()) {
                        Text(
                            text = Strings.SENSE_NOTICIES,
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(s.noticies, key = { it.id }) { noticia ->
                                NoticiaCard(noticia)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulari) {
        CrearNoticiaDialog(
            onDismiss = { mostrarFormulari = false },
            onPublica = { titol, cos, tipus ->
                vm.publicaNoticia(titol, cos, tipus)
                mostrarFormulari = false
            }
        )
    }
}

@Composable
private fun NoticiaCard(noticia: Noticia) {
    val tipus = runCatching { TipusNoticia.valueOf(noticia.tipus) }.getOrDefault(TipusNoticia.GENERAL)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipusChip(tipus)
                Text(
                    text = dataRelativa(noticia.data),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = noticia.titol,
                style = MaterialTheme.typography.titleMedium
            )
            if (noticia.cos.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = noticia.cos,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun TipusChip(tipus: TipusNoticia) {
    val (label, color) = when (tipus) {
        TipusNoticia.ENTRENO -> "Entreno" to MaterialTheme.colorScheme.primaryContainer
        TipusNoticia.COMPETICIO -> "Competició" to MaterialTheme.colorScheme.errorContainer
        TipusNoticia.VIATGE -> "Viatge" to MaterialTheme.colorScheme.tertiaryContainer
        TipusNoticia.GENERAL -> "General" to MaterialTheme.colorScheme.secondaryContainer
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun dataRelativa(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    val ara = Clock.System.now()
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val tz = TimeZone.currentSystemDefault()
    val period = instant.periodUntil(ara, tz)
    return when {
        period.years > 0 -> "fa ${period.years} any${if (period.years > 1) "s" else ""}"
        period.months > 0 -> "fa ${period.months} mes${if (period.months > 1) "os" else ""}"
        period.days > 0 -> "fa ${period.days} dia${if (period.days > 1) "s" else ""}"
        else -> "avui"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearNoticiaDialog(
    onDismiss: () -> Unit,
    onPublica: (titol: String, cos: String, tipus: String) -> Unit
) {
    var titol by remember { mutableStateOf("") }
    var cos by remember { mutableStateOf("") }
    var tipus by remember { mutableStateOf(TipusNoticia.GENERAL) }
    var expandedTipus by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.NOVA_NOTICIA) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titol,
                    onValueChange = { titol = it },
                    label = { Text(Strings.TITOL) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cos,
                    onValueChange = { cos = it },
                    label = { Text(Strings.COS) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                ExposedDropdownMenuBox(
                    expanded = expandedTipus,
                    onExpandedChange = { expandedTipus = it }
                ) {
                    OutlinedTextField(
                        value = tipus.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Strings.TIPUS) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipus) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTipus,
                        onDismissRequest = { expandedTipus = false }
                    ) {
                        TipusNoticia.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.name) },
                                onClick = { tipus = t; expandedTipus = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (titol.isNotBlank()) onPublica(titol, cos, tipus.name) },
                enabled = titol.isNotBlank()
            ) { Text(Strings.PUBLICA) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel·la") }
        }
    )
}
