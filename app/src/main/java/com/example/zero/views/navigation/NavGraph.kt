package com.example.zero.views.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.zero.views.auth.LoginScreen
import com.example.zero.views.camera.CameraScreen
import com.example.zero.views.plant.MyPlantsScreen
import com.example.zero.views.plant.AddPlantScreen
import com.example.zero.views.splash.SplashScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph(navController: NavHostController) {
    // Aseguramos que el gráfico de navegación esté configurado antes de su uso
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "login_screen" &&
                       currentRoute != "add_plant" &&
                       currentRoute != "splash_screen"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "splash_screen",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Pantalla de splash que muestra el logo
            composable("splash_screen") {
                SplashScreen {
                    // Cuando termina el splash, verificamos la autenticación
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        // Si hay usuario autenticado, vamos a la pantalla principal
                        navController.navigate(BottomNavItem.MyPlants.route) {
                            popUpTo("splash_screen") { inclusive = true }
                        }
                    } else {
                        // Si no hay usuario autenticado, vamos a login
                        navController.navigate("login_screen") {
                            popUpTo("splash_screen") { inclusive = true }
                        }
                    }
                }
            }

            // Resto de pantallas de la aplicación
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
