package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.proyecto.data.model.Notificacion
import com.example.proyecto.data.model.TipoNotificacion
import com.example.proyecto.ui.viewmodel.NotificacionViewModel

@OptIn(ExperimentalMaterial3Api::class)

/**
 * Pantalla de notificaciones con integración Realtime.
 * Fase 2: Notificaciones en tiempo real
 */
@Composable
fun NotificacionesScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NotificacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Notificaciones", color = Color.White)
                        if (uiState.noLeidas > 0) {
                            Text(
                                "${uiState.noLeidas} nuevas",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00BFA5)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    if (uiState.noLeidas > 0) {
                        IconButton(onClick = { viewModel.marcarTodasLeidas() }) {
                            Icon(Icons.Filled.DoneAll, contentDescription = "Marcar todas como leídas", tint = Color.White)
                        }
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
            // Indicador de sincronización
            if (uiState.sincronizando) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF3949AB)
                )
            }

            // Mensaje de error
            if (uiState.error != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color(0xFFCF6679).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            uiState.error!!,
                            color = Color(0xFFCF6679),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFFCF6679),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { viewModel.clearError() }
                        )
                    }
                }
            }

            // Contenido
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color(0xFF3949AB)
                    )
                }

                uiState.notificaciones.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.NotificationsNone,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "No tienes notificaciones",
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.notificaciones) { notificacion ->
                            NotificacionCard(
                                notificacion = notificacion,
                                onMarcarLeida = { viewModel.marcarLeida(it) },
                                onEliminar = { viewModel.eliminarNotificacion(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificacionCard(
    notificacion: Notificacion,
    onMarcarLeida: (String) -> Unit = {},
    onEliminar: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (notificacion.leida) Color(0xFF1E1E2E) else Color(0xFF1E1E2E).copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Icono según tipo
                    Icon(
                        imageVector = when (notificacion.tipo) {
                            TipoNotificacion.CONSULTA_NUEVA -> Icons.Filled.Info
                            TipoNotificacion.DOCUMENTO_FIRMADO -> Icons.Filled.CheckCircle
                            TipoNotificacion.ESTADO_CAMBIO -> Icons.Filled.Warning
                            TipoNotificacion.SISTEMA -> Icons.Filled.Info
                        },
                        contentDescription = null,
                        tint = when (notificacion.tipo) {
                            TipoNotificacion.CONSULTA_NUEVA -> Color(0xFF3949AB)
                            TipoNotificacion.DOCUMENTO_FIRMADO -> Color(0xFF00BFA5)
                            TipoNotificacion.ESTADO_CAMBIO -> Color(0xFFFFA726)
                            TipoNotificacion.SISTEMA -> Color(0xFF3949AB)
                        },
                        modifier = Modifier.size(24.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            notificacion.mensaje,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = if (notificacion.leida) FontWeight.Normal else FontWeight.Bold
                        )
                        Text(
                            notificacion.createdAt ?: "Hace poco",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                // Botones de acción
                if (!notificacion.leida) {
                    IconButton(
                        onClick = { onMarcarLeida(notificacion.id ?: "") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Marcar como leída",
                            tint = Color(0xFF00BFA5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onEliminar(notificacion.id ?: "") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Eliminar",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Indicador de no leída
            if (!notificacion.leida) {
                Divider(
                    color = Color(0xFF3949AB),
                    thickness = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
