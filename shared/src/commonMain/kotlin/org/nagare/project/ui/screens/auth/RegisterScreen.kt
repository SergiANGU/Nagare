package org.nagare.project.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.nagare.project.Strings

@Composable
fun RegisterScreen(
    onRegistreSuccess: (uid: String, email: String) -> Unit,
    onAnarALogin: () -> Unit
) {
    val vm: RegisterViewModel = koinViewModel()
    val state by vm.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is RegisterUiState.Success) {
            val s = state as RegisterUiState.Success
            vm.resetState()
            onRegistreSuccess(s.uid, s.email)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = Strings.REGISTRAT,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(Strings.EMAIL) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(Strings.CONTRASENYA) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        if (state is RegisterUiState.Error) {
            Text(
                text = (state as RegisterUiState.Error).missatge,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { vm.signUp(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is RegisterUiState.Loading
        ) {
            if (state is RegisterUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(Strings.REGISTRAT)
            }
        }
        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onAnarALogin) {
            Text(Strings.JA_TENS_COMPTE)
        }
    }
}
