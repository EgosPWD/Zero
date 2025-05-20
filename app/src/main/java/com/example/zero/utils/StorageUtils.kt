package com.example.zero.utils

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*


class StorageUtils {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    suspend fun uploadImage(imageUri: Uri): String {
        // Get current user ID, or throw exception if not logged in
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in to upload images")

        val fileName = UUID.randomUUID().toString()
        // Include user ID in path for proper permission handling
        val storageRef = storage.reference.child("users/$userId/plant_images/$fileName")

        try {
            // Upload file and await completion
            storageRef.putFile(imageUri).await()
            // Return the download URL
            return storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Failed to upload image: ${e.message}", e)
        }
    }
}

