package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.data.model.Notificacion
import com.example.proyecto.ui.viewmodel.NotificacionViewModel

@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0D0D1A),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Notificaciones", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    if (uiState.noLeidas > 0) {
                        IconButton(onClick = { viewModel.marcarTodasLeidas() }) {
                            Icon(Icons.Filled.DoneAll, contentDescription = "Marcar todas leidas", tint = Color(0xFF00BFA5))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E2E))
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF3949AB))
            }
        } else if (uiState.notificaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Notifications, contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No tienes notificaciones", color = Color.White.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.noLeidas > 0) {
                    item {
                        Text("${uiState.noLeidas} sin leer",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF3949AB),
                            fontWeight = FontWeight.SemiBold)
                    }
                }
                items(uiState.notificaciones) { notif ->
                    NotificacionCard(notif, onClick = {
                        if (!notif.leida) notif.id?.let { viewModel.marcarLeida(it) }
                    })
                }
            }
        }
    }
}

@Composable
private fun NotificacionCard(notif: Notificacion, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notif.leida) Color(0xFF3949AB).copy(alpha = 0.1f) else Color(0xFF1E1E2E)
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(8.dp).offset(y = 6.dp)
                    .background(
                        if (!notif.leida) Color(0xFF3949AB) else Color.Transparent,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notif.titulo, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!notif.leida) FontWeight.SemiBold else FontWeight.Normal,
                    color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(notif.mensaje, style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f))
                notif.createdAt?.let { fecha ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(fecha.take(10), style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f))
                }
            }
        }
    }
}
