package com.example.proyecto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

private data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        BottomNavItem("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Abogados", Icons.Filled.People, Icons.Outlined.People),
        BottomNavItem("Documentos", Icons.Filled.Description, Icons.Outlined.Description),
        BottomNavItem("Consultas", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
        BottomNavItem("Perfil", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Scaffold(
        containerColor = Color(0xFF0D0D1A),
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
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon
                                else item.unselectedIcon,
                                contentDescription = item.label
                            )
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
                1 -> LawyersScreen()
                2 -> DocumentsScreen()
                3 -> ConsultationsScreen()
                4 -> ProfileScreen(navController = navController)
            }
        }
    }
}
