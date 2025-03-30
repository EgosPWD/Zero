package com.example.zero.views.auth


import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel()) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val loginState by viewModel.loginState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (loginState) {
            is LoginState.Loading -> CircularProgressIndicator()
            is LoginState.Error -> Text(
                text = (loginState as LoginState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            is LoginState.Success -> Text("Bienvenido, ${(loginState as LoginState.Success).user.email}")
            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.login() }, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.sigUp() }, modifier = Modifier.fillMaxWidth()) {
            Text("Registrarse")
        }
    }
}
