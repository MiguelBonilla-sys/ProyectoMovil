package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Consultation(
    val title: String,
    val lawyerName: String,
    val date: String,
    val status: String,
    val statusColor: Color
)

@Composable
fun ConsultationsScreen() {
    var selectedFilter by remember { mutableStateOf("Activas") }
    var selectedConsultation by remember { mutableStateOf<Consultation?>(null) }

    val filters = listOf("Activas", "Pendientes", "Finalizadas")

    val consultations = listOf(
        Consultation(
            "Despido sin justa causa",
            "Dr. Carlos Ramírez",
            "03 Mar 2026",
            "Activa",
            Color(0xFF00BFA5)
        ),
        Consultation(
            "Divorcio de mutuo acuerdo",
            "Dra. Laura Martínez",
            "28 Feb 2026",
            "Activa",
            Color(0xFF00BFA5)
        ),
        Consultation(
            "Reclamación de seguros",
            "Dr. Andrés García",
            "20 Feb 2026",
            "Pendiente",
            Color(0xFFFFA726)
        ),
        Consultation(
            "Sucesión testamentaria",
            "Dra. María López",
            "10 Ene 2026",
            "Finalizada",
            Color(0xFF757575)
        )
    )

    val filteredConsultations = when (selectedFilter) {
        "Activas" -> consultations.filter { it.status == "Activa" }
        "Pendientes" -> consultations.filter { it.status == "Pendiente" }
        "Finalizadas" -> consultations.filter { it.status == "Finalizada" }
        else -> consultations
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Mis Consultas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gestiona tus asesorías legales",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        // ── Filtros ──
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
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

        // ── Lista de consultas ──
        if (filteredConsultations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay consultas $selectedFilter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        items(filteredConsultations) { consultation ->
            ConsultationCard(consultation, onClick = { selectedConsultation = consultation })
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }

    // ── Diálogo de detalle de consulta ──
    selectedConsultation?.let { c ->
        AlertDialog(
            onDismissRequest = { selectedConsultation = null },
            containerColor = Color(0xFF1E1E2E),
            title = {
                Text(
                    c.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Estado: ", color = Color.White.copy(alpha = 0.7f))
                        Text(c.status, color = c.statusColor, fontWeight = FontWeight.SemiBold)
                    }
                    Text("👨‍⚖️ Abogado: ${c.lawyerName}", color = Color.White.copy(alpha = 0.9f))
                    Text("📅 Fecha: ${c.date}", color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Puedes comunicarte con tu abogado a través del chat integrado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedConsultation = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
                ) { Text("Abrir chat") }
            },
            dismissButton = {
                TextButton(onClick = { selectedConsultation = null }) {
                    Text("Cerrar", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

@Composable
private fun ConsultationCard(consultation: Consultation, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(consultation.statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Gavel,
                        contentDescription = null,
                        tint = consultation.statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = consultation.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = consultation.lawyerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3949AB)
                    )
                }
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            consultation.status,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = consultation.statusColor.copy(alpha = 0.15f),
                        labelColor = consultation.statusColor
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = consultation.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF3949AB)
                    )
                ) {
                    Text("Ver Detalle", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
