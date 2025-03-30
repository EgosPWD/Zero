package com.plantsocial.app.views.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Menu : BottomNavItem("menu", Icons.Default.Home, "Menú")
    object Social : BottomNavItem("social", Icons.Default.People, "Red Social")
    object Camera : BottomNavItem("camera", Icons.Default.Camera, "Cámara")
    object MyPlants : BottomNavItem("my_plants", Icons.Default.Grass, "Mis Plantas")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Ajustes")
}
