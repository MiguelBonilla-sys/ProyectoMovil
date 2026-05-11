package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.data.model.EstadoProcesoFirma
import com.example.proyecto.data.session.SessionManager
import com.example.proyecto.ui.viewmodel.DocumentoDetalleViewModel

@OptIn(ExperimentalMaterial3Api::class)

/**
 * Pantalla de detalle de documento con funcionalidad de firma electrónica (FASE 0).
 *
 * Flujos soportados:
 * 1. Ver documento (lectura)
 * 2. Iniciar proceso de firma (quien subió el documento)
 * 3. Firmar documento (los firmantes)
 * 4. Ver historial de firmas
 */
@Composable
fun DocumentoDetalleScreen(
    documentoId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: DocumentoDetalleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoFirmantes by remember { mutableStateOf(false) }
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var mostrarDialogoHistorial by remember { mutableStateOf(false) }

    LaunchedEffect(documentoId) {
        viewModel.cargarDocumento(documentoId)
    }

    // Limpiar mensajes cuando desaparece la pantalla
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Documento", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E2E)
                )
            )
        },
        containerColor = Color(0xFF0D0D1A)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoadingDocumento -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF3949AB)
                    )
                }

                uiState.error != null -> {
                    ErrorCard(
                        mensaje = uiState.error!!,
                        onCerrar = { viewModel.clearMessages() },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    )
                }

                uiState.documento != null -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Información del documento
                        item {
                            DocumentoInfoCard(
                                documento = uiState.documento!!,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Estado de firma
                        if (uiState.procesoFirma != null) {
                            item {
                                ProcesoFirmaCard(
                                    procesoFirma = uiState.procesoFirma!!,
                                    porcentajeFirma = uiState.porcentajeFirma,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Botón para ver historial de firmas
                            item {
                                Button(
                                    onClick = { mostrarDialogoHistorial = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00BFA5)
                                    )
                                ) {
                                    Icon(Icons.Filled.History, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ver Historial de Firmas")
                                }
                            }
                        }

                        // Botones de acción según permisos
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Botón para iniciar firma (si eres quien subió el doc)
                                if (uiState.usuarioActualPuedeIniciarFirma) {
                                    Button(
                                        onClick = { mostrarDialogoFirmantes = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF3949AB)
                                        )
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Iniciar Firma")
                                    }
                                }

                                // Botón para firmar (si eres firmante)
                                if (uiState.usuarioActualDebeSerFirmante) {
                                    Button(
                                        onClick = { mostrarDialogoConfirmacion = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF00BFA5)
                                        )
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Firmar")
                                    }
                                }
                            }
                        }

                        // Mensaje de éxito
                        if (uiState.firmaIniciadaExitosamente) {
                            item {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = Color(0xFF1E1E2E)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF00BFA5),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            "Proceso iniciado. Redireccionar a CAMERFIRMA...",
                                            color = Color(0xFF00BFA5),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }

    // Diálogos
    if (mostrarDialogoFirmantes) {
        SeleccionarFirmantesDialog(
            usuarioActual = SessionManager.currentUser,
            onConfirmar = { firmantes ->
                viewModel.iniciarFirmaElectronica(firmantes)
                mostrarDialogoFirmantes = false
            },
            onCancelar = { mostrarDialogoFirmantes = false }
        )
    }

    if (mostrarDialogoConfirmacion) {
        ConfirmarFirmaDialog(
            onConfirmar = {
                // TODO: Implementar redirección a CAMERFIRMA
                mostrarDialogoConfirmacion = false
            },
            onRechazar = {
                if (uiState.procesoFirma != null) {
                    viewModel.rechazarFirma(
                        procesoId = uiState.procesoFirma!!.id,
                        motivo = "Rechazado por el usuario"
                    )
                }
                mostrarDialogoConfirmacion = false
            },
            onCancelar = { mostrarDialogoConfirmacion = false }
        )
    }

    if (mostrarDialogoHistorial && uiState.procesoFirma != null) {
        HistorialFirmasDialog(
            firmas = uiState.procesoFirma!!.firmasCompletadas,
            onCerrar = { mostrarDialogoHistorial = false }
        )
    }
}

/**
 * Card con información del documento.
 */
@Composable
private fun DocumentoInfoCard(
    documento: com.example.proyecto.data.model.Documento,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF1E1E2E)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Description,
                    contentDescription = null,
                    tint = Color(0xFF00BFA5),
                    modifier = Modifier.size(32.dp)
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            documento.estadoFirma.display,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (documento.estadoFirma.name) {
                            "PENDIENTE" -> Color(0xFFFFA726)
                            "FIRMADO" -> Color(0xFF00BFA5)
                            "RECHAZADO" -> Color(0xFFCF6679)
                            else -> Color(0xFF3949AB)
                        }
                    )
                )
            }

            Text(
                text = documento.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tipo", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f))
                Text(documento.tipo, style = MaterialTheme.typography.labelSmall, color = Color.White)
            }

            if (documento.createdAt != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Creado", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f))
                    Text(documento.createdAt!!, style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }
    }
}

/**
 * Card con información del proceso de firma.
 */
@Composable
private fun ProcesoFirmaCard(
    procesoFirma: com.example.proyecto.data.model.ProcesoFirma,
    porcentajeFirma: Int,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF1E1E2E)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Proceso de Firma",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            procesoFirma.estado.name,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (procesoFirma.estado) {
                            EstadoProcesoFirma.COMPLETO -> Color(0xFF00BFA5)
                            EstadoProcesoFirma.EN_PROGRESO -> Color(0xFF3949AB)
                            EstadoProcesoFirma.EXPIRADO -> Color(0xFFCF6679)
                            else -> Color(0xFF999999)
                        }
                    )
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { porcentajeFirma / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF00BFA5),
                trackColor = Color(0xFF3949AB)
            )

            Text(
                "$porcentajeFirma% completado",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(0.7f)
            )

            Text(
                "${procesoFirma.firmasCompletadas.size}/${procesoFirma.firmantesRequeridos.size} firmas",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }
}

/**
 * Diálogo para seleccionar firmantes.
 */
@Composable
private fun SeleccionarFirmantesDialog(
    usuarioActual: com.example.proyecto.data.model.User?,
    onConfirmar: (List<String>) -> Unit,
    onCancelar: () -> Unit
) {
    // TODO: Implementar lista de usuarios para seleccionar firmantes
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onCancelar,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color(0xFF1E1E2E), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Seleccionar Firmantes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    "Esta funcionalidad estará disponible en la próxima versión.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.7f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCancelar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { onConfirmar(emptyList()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

/**
 * Diálogo para confirmar firma.
 */
@Composable
private fun ConfirmarFirmaDialog(
    onConfirmar: () -> Unit,
    onRechazar: () -> Unit,
    onCancelar: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onCancelar,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color(0xFF1E1E2E), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Confirmar Firma",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    "¿Deseas proceder con la firma de este documento? Serás redirigido a CAMERFIRMA para completar el proceso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.7f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRechazar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679))
                    ) {
                        Text("Rechazar")
                    }
                    Button(
                        onClick = onCancelar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = onConfirmar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                    ) {
                        Text("Firmar")
                    }
                }
            }
        }
    }
}

/**
 * Diálogo con historial de firmas.
 */
@Composable
private fun HistorialFirmasDialog(
    firmas: List<com.example.proyecto.data.model.FirmaRegistro>,
    onCerrar: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onCerrar,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
                .background(Color(0xFF1E1E2E), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Historial de Firmas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(firmas.size) { index ->
                        val firma = firmas[index]
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color(0xFF0D0D1A)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Firma ${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00BFA5)
                                )
                                Text(
                                    "Fecha: ${firma.selloTiempo}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(0.7f)
                                )
                                Text(
                                    "Estado: ${firma.estado}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onCerrar,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

/**
 * Card de error.
 */
@Composable
private fun ErrorCard(
    mensaje: String,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFFCF6679)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                mensaje,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCerrar, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
            }
        }
    }
}
