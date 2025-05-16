package com.example.zero.views.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel: ViewModel() {

    //variable de autenticación firebase
    private val auth: FirebaseAuth = Firebase.auth

    //el MutableStateFlow se usar para gestionar estados reactivos en UI
    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)


    val email: StateFlow<String> = _email
    val password: StateFlow<String> = _password
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail

    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword

    }

    fun login() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(_email.value, _password.value).await()
                result.user?.let { firebaseUser ->
                    _loginState.value = LoginState.Success(
                        User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            username = firebaseUser.displayName ?: ""
                        )
                    )
                } ?: run {
                    _loginState.value = LoginState.Error("Login failed")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error de autenticación")
            }
        }
    }

    fun sigUp() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val result =
                    auth.createUserWithEmailAndPassword(_email.value, _password.value).await()
                result.user?.let { firebaseUser ->
                    // 1. Crear documento del usuario en Firestore
                    val db = Firebase.firestore
                    val userId = firebaseUser.uid

                    val userData = mapOf(
                        "email" to (firebaseUser.email ?: ""),
                        "createdAt" to System.currentTimeMillis()
                    )

                    db.collection("users").document(userId).set(userData).await()


                    val samplePlant = mapOf(
                        "name" to "Monstera Deliciosa",
                        "description" to "Planta tropical con hojas grandes y divididas",
                        "imageUrl" to "https://picsum.photos/id/13/2500/1667",
                        "addedAt" to System.currentTimeMillis()
                    )

                    db.collection("users").document(userId)
                        .collection("plants")
                        .add(samplePlant).await()

                    _loginState.value = LoginState.Success(
                        User(
                            id = userId,
                            email = firebaseUser.email ?: "",
                            username = firebaseUser.displayName ?: ""
                        )
                    )
                } ?: run {
                    _loginState.value = LoginState.Error("Registro fallido")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error de autenticación")
            }
        }
    }

data class User(
    val id: String,
    val email: String,
    val username: String
)

sealed class LoginState{
    object Initial: LoginState()
    object Loading: LoginState()
    data class Success(val user: User): LoginState()
    data class Error(val message: String): LoginState()

}}