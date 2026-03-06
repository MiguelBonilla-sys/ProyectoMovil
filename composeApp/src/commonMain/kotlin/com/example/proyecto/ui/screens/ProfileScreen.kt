package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.proyecto.navigation.Routes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.example.proyecto.data.session.SessionManager

@Composable
fun ProfileScreen(navController: NavController) {
    var menuDialog by remember { mutableStateOf("") }
    val user = SessionManager.currentUser
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // ── Avatar ──
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF3949AB)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user?.initials ?: "?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user?.nombre ?: "Usuario",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user?.email ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Sección: Mi Cuenta ──
        SectionHeader("Mi Cuenta")
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Filled.Person,
                    label = "Datos Personales",
                    onClick = { menuDialog = "Datos Personales" }
                )
                HorizontalDivider(color = Color(0xFF2A2A3C))
                ProfileMenuItem(
                    icon = Icons.Filled.Lock,
                    label = "Seguridad",
                    onClick = { menuDialog = "Seguridad" }
                )
                HorizontalDivider(color = Color(0xFF2A2A3C))
                ProfileMenuItem(
                    icon = Icons.Filled.Notifications,
                    label = "Notificaciones",
                    onClick = { menuDialog = "Notificaciones" }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Sección: Soporte ──
        SectionHeader("Soporte")
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    label = "Centro de Ayuda",
                    onClick = { menuDialog = "Centro de Ayuda" }
                )
                HorizontalDivider(color = Color(0xFF2A2A3C))
                ProfileMenuItem(
                    icon = Icons.Filled.Description,
                    label = "Términos y Condiciones",
                    onClick = { menuDialog = "Términos y Condiciones" }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Cerrar Sesión ──
        OutlinedButton(
            onClick = {
                SessionManager.logout()
                navController.navigate(Routes.WELCOME) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFCF6679)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCF6679))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Cerrar Sesión",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Versión ──
        Text(
            text = "LexSign v1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // ── Diálogo menú ítem ──
    if (menuDialog.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { menuDialog = "" },
            containerColor = Color(0xFF1E1E2E),
            title = {
                Text(
                    menuDialog,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Esta función estará disponible próximamente.",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = { menuDialog = "" },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
                ) { Text("Entendido") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color(0xFF3949AB),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
