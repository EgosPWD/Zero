package com.example.zero.views.plant

import WeatherResponse
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.navigation.NavController


@Composable
fun MyPlantsScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var plantList by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var loading by remember { mutableStateOf(true) }
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }

    val apiKey = "2f06aaf89c0647c5844191401250205"

    // Obtener plantas y clima al mismo tiempo
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

        try {
            val weatherApi = WeatherApi.create()
            weather = weatherApi.getCurrentWeather(apiKey, "Tarija")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Clima en tu ciudad", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        if (weather != null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Ciudad: ${weather!!.location.name}", style = MaterialTheme.typography.bodyMedium)
                    Text("Clima: ${weather!!.current.condition.text}", style = MaterialTheme.typography.bodyMedium)
                    Text("Temp: ${weather!!.current.temp_c}°C", style = MaterialTheme.typography.bodyMedium)
                }
                AsyncImage(
                    model = "https:${weather!!.current.condition.icon}",
                    contentDescription = "Icono del clima",
                    modifier = Modifier.size(64.dp)
                )
            }
        } else {
            Text("Cargando clima...")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Tus Plantas", style = MaterialTheme.typography.titleMedium)

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
