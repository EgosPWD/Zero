package com.example.zero.views.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Menu : BottomNavItem("menu", Icons.Default.Home, "")
    object Social : BottomNavItem("social", Icons.Default.People, "")
    object Camera : BottomNavItem("camera", Icons.Default.Camera, "")
    object MyPlants : BottomNavItem("my_plants", Icons.Default.Grass, "")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "")
}
