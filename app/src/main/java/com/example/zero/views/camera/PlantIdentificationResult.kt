package com.example.zero.views.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlantIdentificationResult(
    plantName: String?,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = plantName != null || errorMessage != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(
                    color = if (plantName != null) MaterialTheme.colorScheme.primaryContainer
                           else MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                plantName != null -> Text(
                    "Planta Identificada: $plantName",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                errorMessage != null -> Text(
                    "Error: $errorMessage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
