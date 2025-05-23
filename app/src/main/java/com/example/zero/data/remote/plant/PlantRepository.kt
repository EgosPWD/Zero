package com.example.zero.data.remote.plant

import com.example.zero.domain.Plant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlantRepository (
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
){
    suspend fun addPlant(plant: Plant) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("plants")
            .add(plant)
            .await()

    }

    suspend fun getplants(): List<Plant> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("plants")
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            it.toObject(Plant::class.java)?.apply {
                id = it.id // Aseguramos que el id se asigne al objeto Plant
            }
        }
    }

    suspend fun deletePlant(plantId: String) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        firestore.collection("users")
            .document(userId)
            .collection("plants")
            .document(plantId)
            .delete()
            .await()
    }
}
