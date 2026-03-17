package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.data.session.SessionManager
import com.example.proyecto.navigation.Routes
import com.example.proyecto.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.usuario ?: SessionManager.currentUser
    var mostrarEdicion by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessages()
        }
    }

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
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF3949AB).copy(alpha = 0.2f)
        ) {
            Text(
                text = when (user?.tipo) {
                    "abogado" -> "Abogado"
                    "administrador" -> "Administrador"
                    else -> "Cliente"
                },
                color = Color(0xFF3949AB),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        if (uiState.successMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00BFA5).copy(alpha = 0.15f))
            ) {
                Text(
                    uiState.successMessage!!,
                    color = Color(0xFF00BFA5),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFCF6679).copy(alpha = 0.15f))
            ) {
                Text(
                    uiState.error!!,
                    color = Color(0xFFCF6679),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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
                    label = "Editar Datos Personales",
                    onClick = { mostrarEdicion = true }
                )
                HorizontalDivider(color = Color(0xFF2A2A3C))
                ProfileMenuItem(
                    icon = Icons.Filled.Lock,
                    label = "Seguridad",
                    onClick = {}
                )
                HorizontalDivider(color = Color(0xFF2A2A3C))
                ProfileMenuItem(
                    icon = Icons.Filled.Notifications,
                    label = "Notificaciones",
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                    onClick = {}
                )
                HorizontalDivider(color = Color(0xFF2A2A3C))
                ProfileMenuItem(
                    icon = Icons.Filled.Description,
                    label = "Términos y Condiciones",
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = {
                SessionManager.logout()
                navController.navigate(Routes.WELCOME) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCF6679)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCF6679))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "LexSign v1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (mostrarEdicion) {
        EditarPerfilDialog(
            user = user,
            isSaving = uiState.isSaving,
            onDismiss = { mostrarEdicion = false },
            onConfirm = { nombre, telefono, descripcion, especialidad, experiencia ->
                viewModel.actualizarPerfil(nombre, telefono, descripcion, especialidad, experiencia)
                mostrarEdicion = false
            }
        )
    }
}

@Composable
private fun EditarPerfilDialog(
    user: com.example.proyecto.data.model.User?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?, String?, Int?) -> Unit
) {
    var nombre by remember { mutableStateOf(user?.nombre ?: "") }
    var telefono by remember { mutableStateOf(user?.telefono ?: "") }
    var descripcion by remember { mutableStateOf(user?.descripcion ?: "") }
    var especialidad by remember { mutableStateOf(user?.especialidad ?: "") }
    var experiencia by remember { mutableStateOf(user?.experiencia?.toString() ?: "") }

    val isAbogado = user?.tipo == "abogado"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2E),
        title = {
            Text("Editar Perfil", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                if (isAbogado) {
                    OutlinedTextField(
                        value = especialidad,
                        onValueChange = { especialidad = it },
                        label = { Text("Especialidad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    OutlinedTextField(
                        value = experiencia,
                        onValueChange = { experiencia = it },
                        label = { Text("Años de experiencia") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción profesional") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        nombre,
                        telefono.takeIf { it.isNotBlank() },
                        descripcion.takeIf { it.isNotBlank() },
                        especialidad.takeIf { it.isNotBlank() },
                        experiencia.toIntOrNull()
                    )
                },
                enabled = nombre.isNotBlank() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF3949AB),
    unfocusedBorderColor = Color(0xFF424242),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color(0xFF3949AB),
    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
    cursorColor = Color(0xFF3949AB)
)

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
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = Color(0xFF3949AB), modifier = Modifier.size(22.dp))
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
