package com.example.proyecto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.ui.screens.LoginAbogadoScreen
import com.example.proyecto.ui.screens.LoginClienteScreen
import com.example.proyecto.ui.screens.MainScreen
import com.example.proyecto.ui.screens.WelcomeScreen

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN_CLIENTE = "login_cliente"
    const val LOGIN_ABOGADO = "login_abogado"
    const val MAIN = "main"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(navController = navController)
        }
        composable(Routes.LOGIN_CLIENTE) {
            LoginClienteScreen(navController = navController)
        }
        composable(Routes.LOGIN_ABOGADO) {
            LoginAbogadoScreen(navController = navController)
        }
        composable(Routes.MAIN) {
            MainScreen()
        }
    }
}
