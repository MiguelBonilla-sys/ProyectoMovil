package com.example.proyecto.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.proyecto.data.session.SessionManager
import com.example.proyecto.ui.screens.CreateConsultationScreen
import com.example.proyecto.ui.screens.HelpCenterScreen
import com.example.proyecto.ui.screens.LoginAbogadoScreen
import com.example.proyecto.ui.screens.LoginClienteScreen
import com.example.proyecto.ui.screens.MainScreen
import com.example.proyecto.ui.screens.NotificationsScreen
import com.example.proyecto.ui.screens.RegisterAbogadoScreen
import com.example.proyecto.ui.screens.RegisterClienteScreen
import com.example.proyecto.ui.screens.SecurityScreen
import com.example.proyecto.ui.screens.TermsScreen
import com.example.proyecto.ui.screens.WelcomeScreen

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN_CLIENTE = "login_cliente"
    const val LOGIN_ABOGADO = "login_abogado"
    const val REGISTER_CLIENTE = "register_cliente"
    const val REGISTER_ABOGADO = "register_abogado"
    const val MAIN = "main"
    const val CREATE_CONSULTATION = "create_consultation?abogadoId={abogadoId}&abogadoNombre={abogadoNombre}"
    const val SECURITY = "security"
    const val NOTIFICATIONS = "notifications"
    const val HELP_CENTER = "help_center"
    const val TERMS = "terms"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val startDestination = remember {
        if (SessionManager.restoreSession()) Routes.MAIN else Routes.WELCOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.WELCOME) { WelcomeScreen(navController = navController) }
        composable(Routes.LOGIN_CLIENTE) { LoginClienteScreen(navController = navController) }
        composable(Routes.LOGIN_ABOGADO) { LoginAbogadoScreen(navController = navController) }
        composable(Routes.REGISTER_CLIENTE) { RegisterClienteScreen(navController = navController) }
        composable(Routes.REGISTER_ABOGADO) { RegisterAbogadoScreen(navController = navController) }
        composable(Routes.MAIN) { MainScreen(navController = navController) }
        composable(
            route = Routes.CREATE_CONSULTATION,
            arguments = listOf(
                navArgument("abogadoId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("abogadoNombre") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            CreateConsultationScreen(
                abogadoId = backStackEntry.arguments?.getString("abogadoId"),
                abogadoNombre = backStackEntry.arguments?.getString("abogadoNombre"),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SECURITY) { SecurityScreen(navController = navController) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(navController = navController) }
        composable(Routes.HELP_CENTER) { HelpCenterScreen(navController = navController) }
        composable(Routes.TERMS) { TermsScreen(navController = navController) }
    }
}
