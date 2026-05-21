package com.example.proyecto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.proyecto.ui.screens.AuditLogScreen
import com.example.proyecto.ui.screens.CreateConsultationScreen
import com.example.proyecto.ui.screens.DocumentoDetalleScreen
import com.example.proyecto.ui.screens.ForgotPasswordScreen
import com.example.proyecto.ui.screens.HelpCenterScreen
import com.example.proyecto.ui.screens.LoginAbogadoScreen
import com.example.proyecto.ui.screens.LoginClienteScreen
import com.example.proyecto.ui.screens.MainScreen
import com.example.proyecto.ui.screens.NotificacionesScreen
import com.example.proyecto.ui.screens.RegisterAbogadoScreen
import com.example.proyecto.ui.screens.RegisterClienteScreen
import com.example.proyecto.ui.screens.SubirArchivoScreen
import com.example.proyecto.ui.screens.TemplateFormScreen
import com.example.proyecto.data.model.TipoPlantilla
import com.example.proyecto.ui.screens.SecurityScreen
import com.example.proyecto.ui.screens.SplashScreen
import com.example.proyecto.ui.screens.TermsScreen
import com.example.proyecto.ui.screens.WelcomeScreen

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val LOGIN_CLIENTE = "login_cliente"
    const val LOGIN_ABOGADO = "login_abogado"
    const val REGISTER_CLIENTE = "register_cliente"
    const val REGISTER_ABOGADO = "register_abogado"
    const val FORGOT_PASSWORD = "forgot_password"
    const val MAIN = "main"
    const val CREATE_CONSULTATION = "create_consultation?abogadoId={abogadoId}&abogadoNombre={abogadoNombre}"
    const val DOCUMENTO_DETALLE = "documento_detalle/{documentoId}"
    const val SUBIR_ARCHIVO = "subir_archivo"
    const val TEMPLATE_FORM = "template_form/{tipoPlantilla}"
    const val SECURITY = "security"
    const val NOTIFICATIONS = "notifications"
    const val AUDIT_LOGS = "audit_logs"
    const val HELP_CENTER = "help_center"
    const val TERMS = "terms"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) { SplashScreen(navController = navController) }
        composable(Routes.WELCOME) { WelcomeScreen(navController = navController) }
        composable(Routes.LOGIN_CLIENTE) { LoginClienteScreen(navController = navController) }
        composable(Routes.LOGIN_ABOGADO) { LoginAbogadoScreen(navController = navController) }
        composable(Routes.REGISTER_CLIENTE) { RegisterClienteScreen(navController = navController) }
        composable(Routes.REGISTER_ABOGADO) { RegisterAbogadoScreen(navController = navController) }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onRecuperacionExitosa = {
                    navController.navigate(Routes.LOGIN_CLIENTE) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }
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
        composable(
            route = Routes.DOCUMENTO_DETALLE,
            arguments = listOf(
                navArgument("documentoId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            DocumentoDetalleScreen(
                documentoId = backStackEntry.arguments?.getString("documentoId") ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SECURITY) { SecurityScreen(navController = navController) }
        composable(Routes.NOTIFICATIONS) { NotificacionesScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Routes.AUDIT_LOGS) { AuditLogScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Routes.HELP_CENTER) { HelpCenterScreen(navController = navController) }
        composable(Routes.TERMS) { TermsScreen(navController = navController) }
        composable(Routes.SUBIR_ARCHIVO) {
            SubirArchivoScreen(
                onBack = { navController.popBackStack() },
                onArchivoSubido = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Routes.TEMPLATE_FORM,
            arguments = listOf(
                navArgument("tipoPlantilla") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tipoStr = backStackEntry.arguments?.getString("tipoPlantilla") ?: return@composable
            val tipo = try { TipoPlantilla.valueOf(tipoStr) } catch (e: Exception) { return@composable }
            TemplateFormScreen(
                tipoPlantilla = tipo,
                onBack = { navController.popBackStack() },
                onDocumentoCreado = {
                    navController.popBackStack()
                    navController.navigate(Routes.MAIN)
                }
            )
        }
    }
}
