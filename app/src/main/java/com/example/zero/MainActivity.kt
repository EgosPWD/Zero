package com.example.zero

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.zero.*
import postgresql

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var pdo: postgresql


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        auth = Firebase.auth
        pdo = postgresql()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthScreen(auth, this)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this, "Usuario ya autenticado: ${currentUser.email}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun AuthScreen(auth: FirebaseAuth, activity: ComponentActivity) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isLoggedIn) "Bienvenido ${auth.currentUser?.email}" else "Autenticación Firebase",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (!isLoggedIn) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty() && isValidEmail(email)) {
                            isLoading = true
                            errorMessage = ""
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(activity) { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        isLoggedIn = true
                                        Toast.makeText(activity, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                                    } else {
                                        errorMessage = when (task.exception) {
                                            is FirebaseAuthInvalidCredentialsException -> "Credenciales inválidas"
                                            is FirebaseAuthInvalidUserException -> "Usuario no encontrado"
                                            else -> "Error desconocido: ${task.exception?.message}"
                                        }
                                    }
                                }
                        } else {
                            errorMessage = "Por favor complete todos los campos correctamente"
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Iniciar Sesión")
                }

                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            errorMessage = ""
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(activity) { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        isLoggedIn = true
                                        Toast.makeText(activity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                    } else {
                                        errorMessage = "Error: ${task.exception?.message}"
                                    }
                                }
                        } else {
                            errorMessage = "Por favor complete todos los campos"
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Registrarse")
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Text(
                text = "Has iniciado sesión correctamente",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    auth.signOut()
                    isLoggedIn = false
                }
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
