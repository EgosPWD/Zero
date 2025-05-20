package com.example.zero.views.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.zero.views.auth.LoginScreen
import com.example.zero.views.camera.CameraScreen
import com.example.zero.views.plant.MyPlantsScreen
import com.example.zero.views.plant.AddPlantScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    // Aseguramos que el gráfico de navegación esté configurado antes de su uso
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "login_screen" && currentRoute != "add_plant"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login_screen",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login_screen") {
                LoginScreen(navController)
            }
            composable(BottomNavItem.MyPlants.route) {
                MyPlantsScreen(navController)
            }
            composable(BottomNavItem.Camera.route) {
                CameraScreen()
            }
            composable("add_plant") {
                AddPlantScreen(navController)
            }
        }
    }
}
