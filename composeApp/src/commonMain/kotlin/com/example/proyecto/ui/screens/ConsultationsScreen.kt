package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.data.model.Consulta
import com.example.proyecto.data.model.EstadoConsulta
import com.example.proyecto.ui.viewmodel.ConsultaViewModel

private val FILTROS_ESTADO = listOf("Todas", "Abiertas", "Activas", "Cerradas")
private val AREAS = listOf("Civil", "Penal", "Laboral", "Familia", "Comercial", "Administrativo")

@Composable
fun ConsultationsScreen(viewModel: ConsultaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarNuevaConsulta by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0D0D1A),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarNuevaConsulta = true },
                containerColor = Color(0xFF3949AB)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva consulta", tint = Color.White)
            }
        }
    ) { padding ->
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
                            "Consultas",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${uiState.consultas.size} consultas en total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { viewModel.cargarConsultas() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar", tint = Color.White)
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FILTROS_ESTADO) { filtro ->
                        FilterChip(
                            selected = uiState.estadoFiltro == filtro,
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

            when {
                uiState.isLoading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3949AB))
                    }
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
                uiState.consultasFiltradas.isEmpty() -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Gavel,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No hay consultas",
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            TextButton(onClick = { mostrarNuevaConsulta = true }) {
                                Text("Crear primera consulta", color = Color(0xFF3949AB))
                            }
                        }
                    }
                }
                else -> items(uiState.consultasFiltradas) { consulta ->
                    ConsultaCard(consulta = consulta)
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (mostrarNuevaConsulta) {
        NuevaConsultaDialog(
            onDismiss = { mostrarNuevaConsulta = false },
            onConfirm = { area, descripcion ->
                viewModel.crearConsulta(
                    areaPractica = area,
                    descripcion = descripcion,
                    onSuccess = { mostrarNuevaConsulta = false },
                    onError = {}
                )
            }
        )
    }
}

@Composable
private fun ConsultaCard(consulta: Consulta) {
    val (statusColor, statusText) = when (consulta.estado) {
        EstadoConsulta.ABIERTA  -> Color(0xFFFFA726) to "Abierta"
        EstadoConsulta.EN_CURSO -> Color(0xFF00BFA5) to "En Curso"
        EstadoConsulta.CERRADA  -> Color(0xFF757575) to "Cerrada"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = consulta.areaPractica,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = consulta.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            consulta.createdAt?.let { fecha ->
                Text(
                    text = fecha.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuevaConsultaDialog(
    onDismiss: () -> Unit,
    onConfirm: (area: String, descripcion: String) -> Unit
) {
    var area by remember { mutableStateOf(AREAS.first()) }
    var descripcion by remember { mutableStateOf("") }
    var expandedArea by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2E),
        title = {
            Text("Nueva Consulta", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedArea,
                    onExpandedChange = { expandedArea = it }
                ) {
                    OutlinedTextField(
                        value = area,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Área de práctica") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedArea) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3949AB),
                            unfocusedBorderColor = Color(0xFF424242),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF3949AB),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedArea,
                        onDismissRequest = { expandedArea = false },
                        containerColor = Color(0xFF1E1E2E)
                    ) {
                        AREAS.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item, color = Color.White) },
                                onClick = { area = item; expandedArea = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3949AB),
                        unfocusedBorderColor = Color(0xFF424242),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF3949AB),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (descripcion.isNotBlank()) onConfirm(area, descripcion) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB)),
                enabled = descripcion.isNotBlank()
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}
