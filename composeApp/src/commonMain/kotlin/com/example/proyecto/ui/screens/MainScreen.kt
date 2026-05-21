package com.example.proyecto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.data.session.SessionManager
import com.example.proyecto.navigation.Routes
import com.example.proyecto.ui.components.LexSignTopBar
import com.example.proyecto.ui.viewmodel.NotificacionViewModel

private data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(
    navController: NavController,
    notificacionViewModel: NotificacionViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentUser = SessionManager.currentUser
    val isAdmin = currentUser?.esAdmin == true

    val notifState by notificacionViewModel.uiState.collectAsState()
    val noLeidas = notifState.noLeidas

    val navItems = buildList {
        add(BottomNavItem("Inicio", Icons.Filled.Home, Icons.Outlined.Home))
        add(BottomNavItem("Abogados", Icons.Filled.People, Icons.Outlined.People))
        add(BottomNavItem("Documentos", Icons.Filled.Description, Icons.Outlined.Description))
        add(BottomNavItem("Consultas", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat))
        add(BottomNavItem("Perfil", Icons.Filled.Person, Icons.Outlined.Person))
        if (isAdmin) add(BottomNavItem("Reportes", Icons.Filled.Analytics, Icons.Outlined.Analytics))
    }

    Scaffold(
        containerColor = Color(0xFF0D0D1A),
        topBar = {
            LexSignTopBar(navController = navController)
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E1E2E),
                contentColor = Color.White
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            if (index == 4 && noLeidas > 0) {
                                BadgedBox(badge = { Badge { Text(if (noLeidas > 9) "9+" else noLeidas.toString()) } }) {
                                    Icon(
                                        imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            }
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF3949AB),
                            selectedTextColor = Color(0xFF3949AB),
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = Color(0xFF3949AB).copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> HomeScreen(onNavigateToTab = { selectedTab = it })
                1 -> LawyersScreen(
                    onSolicitarConsulta = { abogadoId, abogadoNombre ->
                        navController.navigate("create_consultation?abogadoId=$abogadoId&abogadoNombre=$abogadoNombre")
                    },
                    navController = navController
                )
                2 -> DocumentsScreen(
                    navController = navController,
                    onNavigateSubirArchivo = { navController.navigate(Routes.SUBIR_ARCHIVO) },
                    onNavigateTemplateForm = { tipo -> navController.navigate("template_form/${tipo.name}") }
                )
                3 -> ConsultationsScreen()
                4 -> ProfileScreen(navController = navController)
                5 -> if (isAdmin) ReportesScreen()
            }
        }
    }
}
