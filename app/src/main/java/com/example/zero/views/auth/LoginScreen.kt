package com.example.zero.views.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import com.example.zero.views.auth.LoginViewModel.LoginState
import com.google.firebase.auth.FirebaseAuth


@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val loginState by viewModel.loginState.collectAsState()

    // Comprobamos si el usuario ya está logueado
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    LaunchedEffect(firebaseUser) {
        if (firebaseUser != null) {
            // Si ya está autenticado, redirigimos a la pantalla de plantas
            navController.navigate("my_plants") {
                popUpTo("login_screen") { inclusive = true }
            }
        }
    }

    // Si el usuario no está autenticado, mostramos el formulario de login
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
            is LoginState.Success -> {
                Text("Bienvenido, ${(loginState as LoginState.Success).user.email}")
                // Navegar a la pantalla de plantas cuando el login es exitoso
                LaunchedEffect(Unit) {
                    navController.navigate("my_plants") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                }
            }
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
