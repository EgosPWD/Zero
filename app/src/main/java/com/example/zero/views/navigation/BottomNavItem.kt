package com.example.zero.views.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object MyPlants : BottomNavItem("my_plants", "Mis Plantas", Icons.Default.LocalFlorist)
    object Camera : BottomNavItem("camera", "Cámara", Icons.Default.Camera)
}
