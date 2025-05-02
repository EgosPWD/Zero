package com.example.zero.views.plant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun MyPlantsScreen() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var plantList by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("plants")
                .get()
                .await()

            plantList = snapshot.documents.map { it.data ?: emptyMap() }
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("🌤️ Clima en tu ciudad", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Text("🌱 Tus Plantas", style = MaterialTheme.typography.titleMedium)

        if (loading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(plantList) { plant ->
                    val name = plant["name"] as? String ?: "Sin nombre"
                    val description = plant["description"] as? String ?: "Sin descripción"
                    val imageUrl = plant["imageUrl"] as? String

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (imageUrl != null) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }
                            Text(text = name, style = MaterialTheme.typography.titleMedium)
                            Text(text = description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}