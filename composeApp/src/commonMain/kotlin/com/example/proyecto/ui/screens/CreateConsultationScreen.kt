package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.ui.viewmodel.CreateConsultationViewModel

private val AREAS_PRACTICA = listOf(
    "Civil", "Penal", "Laboral", "Familia", "Comercial", "Administrativo"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateConsultationScreen(
    abogadoId: String? = null,
    abogadoNombre: String? = null,
    onBack: () -> Unit,
    viewModel: CreateConsultationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var areaExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(abogadoId, abogadoNombre) {
        if (!abogadoId.isNullOrBlank() || !abogadoNombre.isNullOrBlank()) {
            viewModel.setAbogado(abogadoId, abogadoNombre)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color(0xFF0D0D1A),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Nueva Consulta", color = Color.White, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E2E))
            )
        },
        bottomBar = {
            Surface(color = Color(0xFF1E1E2E)) {
                Button(
                    onClick = { viewModel.crear(onSuccess = onBack) },
                    enabled = uiState.areaPractica.isNotBlank()
                            && uiState.descripcion.isNotBlank()
                            && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3949AB),
                        disabledContainerColor = Color(0xFF3949AB).copy(alpha = 0.4f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Crear Consulta", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val nombre = uiState.abogadoNombre
            if (!nombre.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00BFA5).copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color(0xFF00BFA5),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Abogado: $nombre",
                            color = Color(0xFF00BFA5),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Text("Área de Práctica", color = Color.White, style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = areaExpanded,
                onExpandedChange = { areaExpanded = !areaExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.areaPractica.ifBlank { "Seleccionar área..." },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3949AB),
                        unfocusedBorderColor = Color(0xFF424242),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = if (uiState.areaPractica.isBlank()) Color.White.copy(alpha = 0.4f) else Color.White,
                        cursorColor = Color(0xFF3949AB)
                    )
                )
                ExposedDropdownMenu(
                    expanded = areaExpanded,
                    onDismissRequest = { areaExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E1E2E))
                ) {
                    AREAS_PRACTICA.forEach { area ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    area,
                                    color = if (uiState.areaPractica == area) Color(0xFF3949AB) else Color.White
                                )
                            },
                            onClick = { viewModel.setArea(area); areaExpanded = false }
                        )
                    }
                }
            }

            Text("Descripción", color = Color.White, style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = { viewModel.setDescripcion(it) },
                placeholder = { Text("Describe tu consulta con detalle...", color = Color.White.copy(alpha = 0.4f)) },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3949AB),
                    unfocusedBorderColor = Color(0xFF424242),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF3949AB)
                )
            )
        }
    }
}
