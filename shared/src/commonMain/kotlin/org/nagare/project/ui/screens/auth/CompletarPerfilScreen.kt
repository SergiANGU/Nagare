package org.nagare.project.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.nagare.project.Strings

@Composable
fun CompletarPerfilScreen(
    email: String,
    onPerfilDesat: () -> Unit
) {
    val vm: CompletarPerfilViewModel = koinViewModel()
    val state by vm.uiState.collectAsState()

    var nom by remember { mutableStateOf("") }
    var cognoms by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var dataNaixement by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is CompletarPerfilUiState.Success) {
            vm.resetState()
            onPerfilDesat()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = Strings.COMPLETA_PERFIL,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text(Strings.NOM) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = cognoms,
            onValueChange = { cognoms = it },
            label = { Text(Strings.COGNOMS) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = dni,
            onValueChange = { dni = it },
            label = { Text(Strings.DNI) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("8 dígits + lletra (ex: 12345678Z)") }
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = dataNaixement,
            onValueChange = { dataNaixement = it },
            label = { Text(Strings.DATA_NAIXEMENT) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("Format: dd/MM/yyyy") }
        )
        Spacer(Modifier.height(8.dp))

        if (state is CompletarPerfilUiState.Error) {
            Text(
                text = (state as CompletarPerfilUiState.Error).missatge,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { vm.desa(nom, cognoms, dni, dataNaixement, email) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is CompletarPerfilUiState.Loading
        ) {
            if (state is CompletarPerfilUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(Strings.DESA)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
