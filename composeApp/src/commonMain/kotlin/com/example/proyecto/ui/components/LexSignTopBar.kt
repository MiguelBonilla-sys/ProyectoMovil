package com.example.proyecto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.data.session.SessionManager
import com.example.proyecto.navigation.Routes

private val TopBarBg   = Color(0xFF1E1E2E)
private val AccentBlue = Color(0xFF3949AB)
private val TextWhite  = Color.White
private val TextMuted  = Color.White.copy(alpha = 0.65f)
private val DangerRed  = Color(0xFFEF5350)

/**
 * Barra de encabezado global de LexSign.
 *
 * Muestra: logo + "LexSign" + nombre del usuario en sesión + menú desbordante (3 ítems requeridos por el TFC).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LexSignTopBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val currentUser = SessionManager.currentUser
    val nombreUsuario = currentUser?.nombre ?: ""

    var menuExpanded by remember { mutableStateOf(false) }
    var showDialog   by remember { mutableStateOf(0) } // 0=ninguno 1=equipo 2=acerca

    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = TopBarBg),
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AccentBlue),
                contentAlignment = Alignment.Center
            ) {
                LexSignLogo(modifier = Modifier.size(22.dp), color = TextWhite)
            }
        },
        title = {
            Column(modifier = Modifier.padding(start = 6.dp)) {
                Text(
                    text = "LexSign",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                if (nombreUsuario.isNotEmpty()) {
                    Text(
                        text = nombreUsuario,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menú",
                        tint = TextWhite
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    // ── Ítem 1: Equipo de desarrollo ──────────────────
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Default.Group, contentDescription = null,
                                tint = AccentBlue)
                        },
                        text = { Text("Equipo de desarrollo") },
                        onClick = { menuExpanded = false; showDialog = 1 }
                    )

                    // ── Ítem 2: Acerca de la app ──────────────────────
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Default.Info, contentDescription = null,
                                tint = AccentBlue)
                        },
                        text = { Text("Acerca de LexSign") },
                        onClick = { menuExpanded = false; showDialog = 2 }
                    )

                    HorizontalDivider()

                    // ── Ítem 3: Salir ─────────────────────────────────
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null, tint = DangerRed)
                        },
                        text = { Text("Salir", color = DangerRed) },
                        onClick = {
                            menuExpanded = false
                            SessionManager.logout()
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    )

    // ── Diálogo 1: Equipo ─────────────────────────────────────────────
    if (showDialog == 1) {
        AlertDialog(
            onDismissRequest = { showDialog = 0 },
            title = { Text("Equipo de desarrollo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentBlue)
                                .align(Alignment.CenterVertically)
                        )
                        Text("Miguel Bonilla")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentBlue)
                                .align(Alignment.CenterVertically)
                        )
                        Text("Sebastian Fandiño")
                    }
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Aplicación: LexSign",
                        fontWeight = FontWeight.SemiBold,
                        color = AccentBlue
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = 0 }) { Text("Cerrar") }
            }
        )
    }

    // ── Diálogo 2: Acerca de ──────────────────────────────────────────
    if (showDialog == 2) {
        AlertDialog(
            onDismissRequest = { showDialog = 0 },
            title = { Text("Acerca de LexSign", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Plataforma móvil que conecta clientes con abogados certificados en Colombia. " +
                        "Permite gestionar consultas legales, firmar documentos digitalmente y hacer " +
                        "seguimiento de casos en tiempo real."
                    )
                    HorizontalDivider()
                    InfoRow("Asignatura", "Desarrollo de Aplicaciones Móviles")
                    InfoRow("Tipo", "Trabajo Final de Curso — TFC")
                    InfoRow("Periodo", "2026-1")
                    InfoRow("Tecnología", "Kotlin Multiplatform (KMP)")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = 0 }) { Text("Cerrar") }
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium)
    }
}
