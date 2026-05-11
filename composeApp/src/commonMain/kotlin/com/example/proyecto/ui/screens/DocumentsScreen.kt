package com.example.proyecto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
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
import com.example.proyecto.data.model.TipoPlantilla
import com.example.proyecto.data.session.SessionManager
import com.example.proyecto.ui.viewmodel.DocumentoViewModel

private val FILTROS = listOf("Todos", "Pendientes", "Firmados", "Rechazados")

private val PLANTILLAS_PREDEFINIDAS = listOf(
    TipoPlantilla.PODER_NOTARIAL to "Poder Notarial General",
    TipoPlantilla.CONTRATO_ARRENDAMIENTO to "Contrato de Arrendamiento",
    TipoPlantilla.CARTA_AUTORIZACION to "Carta de Autorización",
    TipoPlantilla.NDA to "Acuerdo de Confidencialidad (NDA)",
    TipoPlantilla.DEMANDA_CIVIL to "Demanda Civil"
)

@Composable
fun DocumentsScreen(
    viewModel: DocumentoViewModel = viewModel(),
    navController: androidx.navigation.NavController,
    onNavigateSubirArchivo: () -> Unit,
    onNavigateTemplateForm: (TipoPlantilla) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var vistaPlantillas by remember { mutableStateOf(false) }
    var mostrarMenuCrear by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = Color(0xFF0D0D1A),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarMenuCrear = true },
                containerColor = Color(0xFF3949AB),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Crear documento")
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
                    items(PLANTILLAS_PREDEFINIDAS) { (tipo, nombre) ->
                        PlantillaCard(
                            tipo = tipo,
                            nombre = nombre,
                            onUsarPlantilla = {
                                onNavigateTemplateForm(tipo)
                            }
                        )
                    }
                }
                else -> {
                    val docs = viewModel.documentosFiltrados()
                    if (docs.isEmpty()) {
                        item { EmptyState("No hay documentos") }
                    } else {
                        items(docs) { doc ->
                            DocumentoCard(
                                documento = doc,
                                onFirmar = { id -> viewModel.actualizarFirma(id, EstadoFirma.FIRMADO) { } },
                                onRechazar = { id -> viewModel.actualizarFirma(id, EstadoFirma.RECHAZADO) { } },
                                onEliminar = { id, url -> viewModel.eliminarDocumento(id, url) },
                                onDescargar = null
                            )
                        }
                        item {
                            if (uiState.isLoadingMore) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF3949AB),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else if (uiState.hasMore) {
                                TextButton(
                                    onClick = { viewModel.cargarMas() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cargar más", color = Color(0xFF3949AB))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (mostrarMenuCrear) {
        AlertDialog(
            onDismissRequest = { mostrarMenuCrear = false },
            containerColor = Color(0xFF1E1E2E),
            title = { Text("Agregar documento", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        onClick = {
                            mostrarMenuCrear = false
                            onNavigateSubirArchivo()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3949AB).copy(alpha = 0.15f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Filled.UploadFile, contentDescription = null, tint = Color(0xFF3949AB))
                            Column {
                                Text("Subir archivo", color = Color.White, fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("PDF, DOC, DOCX, TXT",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Card(
                        onClick = { mostrarMenuCrear = false; vistaPlantillas = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF00BFA5).copy(alpha = 0.12f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Filled.Folder, contentDescription = null, tint = Color(0xFF00BFA5))
                            Column {
                                Text("Crear con plantilla", color = Color.White, fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("Formulario legal predefinido",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarMenuCrear = false }) {
                    Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

@Composable
private fun PlantillaCard(
    tipo: TipoPlantilla,
    nombre: String,
    onUsarPlantilla: () -> Unit
) {
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
                Icons.Filled.Folder,
                contentDescription = null,
                tint = Color(0xFF00BFA5),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    tipo.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF00BFA5).copy(alpha = 0.15f)
                ) {
                    Text(
                        "Plantilla",
                        color = Color(0xFF00BFA5),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            IconButton(onClick = onUsarPlantilla) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Usar plantilla",
                    tint = Color(0xFF00BFA5)
                )
            }
        }
    }
}

@Composable
private fun DocumentoCard(
    documento: Documento,
    onFirmar: ((String) -> Unit)? = null,
    onRechazar: ((String) -> Unit)? = null,
    onEliminar: ((String, String) -> Unit)? = null,
    onDescargar: (() -> Unit)? = null
) {
    val (statusColor, statusText) = when (documento.estadoFirma) {
        EstadoFirma.PENDIENTE  -> Color(0xFFFFA726) to "Pendiente"
        EstadoFirma.FIRMADO    -> Color(0xFF00BFA5) to "Firmado"
        EstadoFirma.RECHAZADO  -> Color(0xFFCF6679) to "Rechazado"
    }

    val currentUserId = SessionManager.currentUser?.id
    val showSignActions = documento.estadoFirma == EstadoFirma.PENDIENTE
        && documento.subidoPor != currentUserId
        && onFirmar != null
        && onRechazar != null
    val showDelete = documento.subidoPor == currentUserId && onEliminar != null

    var pendingAction by remember { mutableStateOf<String?>(null) }

    if (pendingAction != null) {
        val action = pendingAction!!
        val (dialogTitle, dialogText, btnColor, btnLabel) = when (action) {
            "firmar"   -> listOf("Confirmar firma", "¿Deseas marcar como firmado \"${documento.nombre}\"?", Color(0xFF00BFA5), "Firmar")
            "rechazar" -> listOf("Confirmar rechazo", "¿Deseas rechazar \"${documento.nombre}\"?", Color(0xFFCF6679), "Rechazar")
            else       -> listOf("Eliminar documento", "¿Eliminar \"${documento.nombre}\" permanentemente?", Color(0xFFCF6679), "Eliminar")
        }
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            containerColor = Color(0xFF1E1E2E),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.85f),
            title = { Text(dialogTitle as String, fontWeight = FontWeight.SemiBold) },
            text = { Text(dialogText as String) },
            confirmButton = {
                Button(
                    onClick = {
                        val docId = documento.id ?: return@Button
                        when (action) {
                            "firmar"   -> onFirmar?.invoke(docId)
                            "rechazar" -> onRechazar?.invoke(docId)
                            "eliminar" -> onEliminar?.invoke(docId, documento.url)
                        }
                        pendingAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor as Color)
                ) { Text(btnLabel as String, color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    tint = Color(0xFF3949AB),
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
                if (onDescargar != null) {
                    IconButton(onClick = onDescargar) {
                        Icon(Icons.Filled.Download, contentDescription = "Descargar", tint = Color(0xFF3949AB))
                    }
                }
                if (showDelete) {
                    IconButton(onClick = { pendingAction = "eliminar" }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar documento", tint = Color(0xFFCF6679))
                    }
                }
            }

            if (showSignActions) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White.copy(alpha = 0.08f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { pendingAction = "firmar" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Firmar", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { pendingAction = "rechazar" },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color(0xFFCF6679)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Rechazar", color = Color(0xFFCF6679), fontWeight = FontWeight.SemiBold)
                    }
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