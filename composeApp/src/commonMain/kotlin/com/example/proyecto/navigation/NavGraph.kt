package com.example.proyecto.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.data.session.SessionManager
import com.example.proyecto.ui.screens.LoginAbogadoScreen
import com.example.proyecto.ui.screens.LoginClienteScreen
import com.example.proyecto.ui.screens.MainScreen
import com.example.proyecto.ui.screens.RegisterAbogadoScreen
import com.example.proyecto.ui.screens.RegisterClienteScreen
import com.example.proyecto.ui.screens.WelcomeScreen

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN_CLIENTE = "login_cliente"
    const val LOGIN_ABOGADO = "login_abogado"
    const val REGISTER_CLIENTE = "register_cliente"
    const val REGISTER_ABOGADO = "register_abogado"
    const val MAIN = "main"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    // Restaura sesión guardada en disco; si hay usuario → arranca directo en MAIN
    val startDestination = remember {
        if (SessionManager.restoreSession()) Routes.MAIN else Routes.WELCOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
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
        composable(Routes.REGISTER_CLIENTE) {
            RegisterClienteScreen(navController = navController)
        }
        composable(Routes.REGISTER_ABOGADO) {
            RegisterAbogadoScreen(navController = navController)
        }
        composable(Routes.MAIN) {
            MainScreen(navController = navController)
        }
    }
}
