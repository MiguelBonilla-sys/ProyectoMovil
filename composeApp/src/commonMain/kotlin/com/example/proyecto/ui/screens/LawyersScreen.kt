package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.proyecto.data.model.User
import com.example.proyecto.ui.viewmodel.AbogadoViewModel

private val ESPECIALIDADES = listOf("Todos", "Penal", "Civil", "Laboral", "Familia", "Comercial")

@Composable
fun LawyersScreen(
    onSolicitarConsulta: (abogadoId: String, abogadoNombre: String) -> Unit = { _, _ -> },
    navController: NavController? = null,
    viewModel: AbogadoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedAbogado by remember { mutableStateOf<User?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
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
                        text = "Abogados",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Encuentra al profesional ideal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = { viewModel.cargarAbogados() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Recargar", tint = Color.White)
                }
            }
        }

        item {
            OutlinedTextField(
                value = uiState.busqueda,
                onValueChange = { viewModel.buscar(it) },
                placeholder = { Text("Buscar abogado...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3949AB),
                    unfocusedBorderColor = Color(0xFF424242),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF3949AB),
                    focusedLeadingIconColor = Color(0xFF3949AB),
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                )
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ESPECIALIDADES) { especialidad ->
                    FilterChip(
                        selected = uiState.especialidadSeleccionada == especialidad,
                        onClick = { viewModel.filtrarPorEspecialidad(especialidad) },
                        label = { Text(especialidad) },
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
                        text = "Error: ${uiState.error}",
                        color = Color(0xFFCF6679),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            uiState.abogadosFiltrados.isEmpty() -> item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No se encontraron abogados",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
            else -> items(uiState.abogadosFiltrados) { abogado ->
                AbogadoCard(abogado = abogado, onClick = { selectedAbogado = abogado })
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }

    selectedAbogado?.let { abogado ->
        AlertDialog(
            onDismissRequest = { selectedAbogado = null },
            containerColor = Color(0xFF1E1E2E),
            title = {
                Text(
                    abogado.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!abogado.especialidad.isNullOrBlank()) {
                        Text("Especialidad: ${abogado.especialidad}", color = Color.White.copy(alpha = 0.9f))
                    }
                    abogado.calificacionPromedio?.let {
                        Text("Calificación: $it / 5.0", color = Color(0xFFFFA726))
                    }
                    abogado.experiencia?.let {
                        Text("Experiencia: $it años", color = Color.White.copy(alpha = 0.7f))
                    }
                    abogado.descripcion?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    }
                    abogado.telefono?.let {
                        Text("Tel: $it", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSolicitarConsulta(abogado.id ?: "", abogado.nombre)
                        selectedAbogado = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
                ) { Text("Solicitar consulta") }
            },
            dismissButton = {
                TextButton(onClick = { selectedAbogado = null }) {
                    Text("Cerrar", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

@Composable
private fun AbogadoCard(abogado: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3949AB)),
                contentAlignment = Alignment.Center
            ) {
                if (!abogado.fotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(abogado.fotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = abogado.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = abogado.initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = abogado.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = abogado.especialidad ?: "Abogado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF3949AB)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = abogado.calificacionPromedio?.let { "%.1f".format(it) } ?: "N/A",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    abogado.experiencia?.let { exp ->
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$exp años exp.",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3949AB))
            ) {
                Text("Ver", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
