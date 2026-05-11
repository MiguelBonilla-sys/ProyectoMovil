package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.data.model.AuditLog
import com.example.proyecto.ui.viewmodel.AuditLogViewModel

@OptIn(ExperimentalMaterial3Api::class)

/**
 * Pantalla de auditoría (solo para administradores).
 * Muestra logs de cambios en el sistema.
 * Fase 2: Auditoría y Seguridad
 */
@Composable
fun AuditLogScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AuditLogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDetalles by remember { mutableStateOf<AuditLog?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auditoría del Sistema", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportarCSV() }) {
                        Icon(Icons.Filled.Download, contentDescription = "Descargar CSV", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E2E)
                )
            )
        },
        containerColor = Color(0xFF0D0D1A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mensajes
            if (uiState.error != null) {
                ErrorBanner(
                    mensaje = uiState.error!!,
                    onCerrar = { viewModel.clearMessages() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            if (uiState.successMessage != null) {
                SuccessBanner(
                    mensaje = uiState.successMessage!!,
                    onCerrar = { viewModel.clearMessages() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            // Contenido
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color(0xFF3949AB)
                    )
                }

                uiState.logs.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay registros de auditoría",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.logs) { log ->
                            AuditLogItem(
                                log = log,
                                onClick = { mostrarDetalles = log },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog de detalles
    if (mostrarDetalles != null) {
        DetallesDialog(
            log = mostrarDetalles!!,
            onCerrar = { mostrarDetalles = null }
        )
    }
}

@Composable
private fun AuditLogItem(
    log: AuditLog,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (log.accion) {
                            "INSERT" -> Icons.Filled.Add
                            "UPDATE" -> Icons.Filled.Edit
                            "DELETE" -> Icons.Filled.Delete
                            else -> Icons.Filled.Info
                        },
                        contentDescription = null,
                        tint = when (log.accion) {
                            "INSERT" -> Color(0xFF00BFA5)
                            "UPDATE" -> Color(0xFF3949AB)
                            "DELETE" -> Color(0xFFCF6679)
                            else -> Color.White
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            log.tablaAfectada.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            log.descripcion,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(0.7f)
                        )
                    }
                }
                AssistChip(
                    onClick = {},
                    label = { Text(log.accion, style = MaterialTheme.typography.labelSmall) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "ID: ${log.registroId.take(8)}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.5f)
                )
                Text(
                    log.createdAt ?: "Sin fecha",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.5f)
                )
            }
        }
    }
}

@Composable
private fun DetallesDialog(
    log: AuditLog,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Detalles del Cambio", color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetalleItem("Tabla", log.tablaAfectada)
                DetalleItem("Acción", log.accion)
                DetalleItem("ID Registro", log.registroId)
                DetalleItem("Usuario", log.usuarioId ?: "SISTEMA")
                DetalleItem("IP", log.ipOrigen ?: "N/A")
                DetalleItem("Fecha", log.createdAt ?: "N/A")
            }
        },
        confirmButton = {
            Button(
                onClick = onCerrar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3949AB)
                )
            ) {
                Text("Cerrar")
            }
        },
        containerColor = Color(0xFF1E1E2E)
    )
}

@Composable
private fun DetalleItem(
    label: String,
    valor: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(0.7f)
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

@Composable
private fun ErrorBanner(
    mensaje: String,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFCF6679).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = null,
                    tint = Color(0xFFCF6679),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    mensaje,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCF6679)
                )
            }
            IconButton(onClick = onCerrar) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color(0xFFCF6679))
            }
        }
    }
}

@Composable
private fun SuccessBanner(
    mensaje: String,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF00BFA5).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF00BFA5),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    mensaje,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF00BFA5)
                )
            }
            IconButton(onClick = onCerrar) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color(0xFF00BFA5))
            }
        }
    }
}
