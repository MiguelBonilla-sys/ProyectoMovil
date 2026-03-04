package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Document(
    val name: String,
    val type: String,
    val date: String,
    val status: String,
    val statusColor: Color,
    val icon: ImageVector
)

@Composable
fun DocumentsScreen() {
    var selectedFilter by remember { mutableStateOf("Todos") }

    val filters = listOf("Todos", "Contratos", "Demandas", "Poderes")

    val documents = listOf(
        Document(
            "Contrato de arrendamiento",
            "Contratos",
            "28 Feb 2026",
            "Firmado",
            Color(0xFF00BFA5),
            Icons.Filled.Description
        ),
        Document(
            "Demanda laboral — caso Rodríguez",
            "Demandas",
            "15 Feb 2026",
            "Pendiente",
            Color(0xFFFFA726),
            Icons.AutoMirrored.Filled.InsertDriveFile
        ),
        Document(
            "Poder especial — representación",
            "Poderes",
            "10 Feb 2026",
            "Firmado",
            Color(0xFF00BFA5),
            Icons.Filled.Folder
        ),
        Document(
            "Contrato prestación de servicios",
            "Contratos",
            "01 Feb 2026",
            "En revisión",
            Color(0xFF42A5F5),
            Icons.Filled.Description
        )
    )

    val filteredDocs = if (selectedFilter == "Todos") documents
    else documents.filter { it.type == selectedFilter }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D1A))) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
                            text = "Mis Documentos",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${documents.size} documentos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    FloatingActionButton(
                        onClick = { /* TODO: subir documento */ },
                        containerColor = Color(0xFF3949AB),
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Subir documento")
                    }
                }
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

            // ── Lista de documentos ──
            items(filteredDocs) { doc ->
                DocumentCard(doc)
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun DocumentCard(document: Document) {
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
                document.icon,
                contentDescription = null,
                tint = Color(0xFF3949AB),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = document.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        document.status,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = document.statusColor.copy(alpha = 0.15f),
                    labelColor = document.statusColor
                ),
                modifier = Modifier.height(28.dp)
            )
        }
    }
}
