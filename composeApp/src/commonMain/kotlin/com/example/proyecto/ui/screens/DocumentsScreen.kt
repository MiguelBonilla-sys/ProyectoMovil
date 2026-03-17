package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.data.model.Documento
import com.example.proyecto.data.model.EstadoFirma
import com.example.proyecto.ui.viewmodel.DocumentoViewModel

private val FILTROS = listOf("Todos", "Pendientes", "Firmados", "Rechazados")

@Composable
fun DocumentsScreen(viewModel: DocumentoViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var vistaPlantillas by remember { mutableStateOf(false) }

    Scaffold(containerColor = Color(0xFF0D0D1A)) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Documentos",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${uiState.documentos.size} documentos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { viewModel.cargarDocumentos() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar", tint = Color.White)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !vistaPlantillas,
                        onClick = { vistaPlantillas = false },
                        label = { Text("Mis Documentos") },
                        leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF3949AB),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E1E2E),
                            labelColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                    FilterChip(
                        selected = vistaPlantillas,
                        onClick = { vistaPlantillas = true },
                        label = { Text("Plantillas") },
                        leadingIcon = { Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00BFA5),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E1E2E),
                            labelColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            if (!vistaPlantillas) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FILTROS) { filtro ->
                            FilterChip(
                                selected = uiState.filtroEstado == filtro,
                                onClick = { viewModel.filtrarPorEstado(filtro) },
                                label = { Text(filtro) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3949AB),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E1E2E),
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }
            }

            when {
                uiState.isLoading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = Color(0xFF3949AB)) }
                }
                uiState.error != null -> item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B1B))
                    ) {
                        Text(
                            "Error: ${uiState.error}",
                            color = Color(0xFFCF6679),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                vistaPlantillas -> {
                    if (uiState.plantillas.isEmpty()) {
                        item {
                            EmptyState("No hay plantillas disponibles")
                        }
                    } else {
                        items(uiState.plantillas) { doc ->
                            DocumentoCard(documento = doc, esPlantilla = true)
                        }
                    }
                }
                else -> {
                    val docs = viewModel.documentosFiltrados()
                    if (docs.isEmpty()) {
                        item { EmptyState("No hay documentos") }
                    } else {
                        items(docs) { doc ->
                            DocumentoCard(documento = doc)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun DocumentoCard(documento: Documento, esPlantilla: Boolean = false) {
    val (statusColor, statusText) = if (esPlantilla) {
        Color(0xFF00BFA5) to "Plantilla"
    } else {
        when (documento.estadoFirma) {
            EstadoFirma.PENDIENTE  -> Color(0xFFFFA726) to "Pendiente"
            EstadoFirma.FIRMADO    -> Color(0xFF00BFA5) to "Firmado"
            EstadoFirma.RECHAZADO  -> Color(0xFFCF6679) to "Rechazado"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (esPlantilla) Icons.Filled.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = if (esPlantilla) Color(0xFF00BFA5) else Color(0xFF3949AB),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    documento.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    documento.tipo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            if (esPlantilla) {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = "Descargar plantilla",
                        tint = Color(0xFF00BFA5)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(mensaje: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(mensaje, color = Color.White.copy(alpha = 0.5f))
    }
}
