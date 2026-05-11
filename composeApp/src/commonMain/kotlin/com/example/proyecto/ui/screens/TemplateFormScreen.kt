package com.example.proyecto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.proyecto.data.model.CampoPlantilla
import com.example.proyecto.data.model.TipoPlantilla
import com.example.proyecto.data.util.TemplateDefinitions
import com.example.proyecto.ui.viewmodel.DocumentoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateFormScreen(
    tipoPlantilla: TipoPlantilla,
    onBack: () -> Unit,
    onDocumentoCreado: () -> Unit,
    viewModel: DocumentoViewModel = viewModel()
) {
    val campos = remember(tipoPlantilla) { TemplateDefinitions.getCampos(tipoPlantilla) }
    var valores by remember { mutableStateOf(emptyMap<String, String>()) }
    var errores by remember { mutableStateOf(emptyMap<String, String>()) }
    var datePickerVisible by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF3949AB),
        unfocusedBorderColor = Color(0xFF424242),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color(0xFF3949AB),
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        cursorColor = Color(0xFF3949AB),
        focusedContainerColor = Color(0xFF1E1E2E),
        unfocusedContainerColor = Color(0xFF1E1E2E)
    )

    Scaffold(
        containerColor = Color(0xFF0D0D1A),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(tipoPlantilla.displayName, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E2E))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Complete los datos del documento",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            itemsIndexed(campos) { _, campo ->
                CampoFormulario(
                    campo = campo,
                    valor = valores[campo.clave] ?: "",
                    error = errores[campo.clave],
                    onValorChange = { nuevoValor ->
                        valores = valores + (campo.clave to nuevoValor)
                        if (errores.containsKey(campo.clave)) {
                            errores = errores - campo.clave
                        }
                    },
                    onShowDatePicker = { datePickerVisible = campo.clave },
                    fieldColors = fieldColors
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val nuevosErrores = validarCampos(campos, valores)
                        if (nuevosErrores.isEmpty()) {
                            isGenerating = true
                            val nombreDoc = "${tipoPlantilla.displayName} - ${System.currentTimeMillis()}"
                            viewModel.generarPdfDesdePlantilla(tipoPlantilla, valores, nombreDoc) { ok, err ->
                                scope.launch {
                                    isGenerating = false
                                    snackbarHostState.showSnackbar(
                                        if (ok) "Documento PDF generado exitosamente" else (err ?: "Error al generar")
                                    )
                                    if (ok) onDocumentoCreado()
                                }
                            }
                        } else {
                            errores = nuevosErrores
                        }
                    },
                    enabled = !isGenerating,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Generar PDF", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (datePickerVisible != null) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { datePickerVisible = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))
                            valores = valores + (datePickerVisible!! to sdf.format(Date(millis)))
                        }
                        datePickerVisible = null
                    }
                ) {
                    Text("Aceptar", color = Color(0xFF3949AB))
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerVisible = null }) {
                    Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}

@Composable
private fun CampoFormulario(
    campo: CampoPlantilla,
    valor: String,
    error: String?,
    onValorChange: (String) -> Unit,
    onShowDatePicker: () -> Unit,
    fieldColors: TextFieldColors
) {
    Column {
        when (campo.tipo) {
            "textarea" -> {
                OutlinedTextField(
                    value = valor,
                    onValueChange = onValorChange,
                    label = { Text(campo.etiqueta + if (campo.requerido) " *" else "") },
                    placeholder = { Text(campo.placeholder, color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    minLines = 3,
                    maxLines = 6
                )
            }
            "fecha" -> {
                OutlinedTextField(
                    value = valor,
                    onValueChange = {},
                    label = { Text(campo.etiqueta + if (campo.requerido) " *" else "") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = onShowDatePicker) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = "Seleccionar fecha",
                                tint = Color(0xFF3949AB)
                            )
                        }
                    }
                )
            }
            "numero" -> {
                OutlinedTextField(
                    value = valor,
                    onValueChange = { if (it.all { c -> c.isDigit() }) onValorChange(it) },
                    label = { Text(campo.etiqueta + if (campo.requerido) " *" else "") },
                    placeholder = { Text(campo.placeholder, color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            else -> {
                OutlinedTextField(
                    value = valor,
                    onValueChange = onValorChange,
                    label = { Text(campo.etiqueta + if (campo.requerido) " *" else "") },
                    placeholder = { Text(campo.placeholder, color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    singleLine = true
                )
            }
        }
        if (error != null) {
            Text(
                error,
                color = Color(0xFFCF6679),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

private fun validarCampos(campos: List<CampoPlantilla>, valores: Map<String, String>): Map<String, String> {
    val errores = mutableMapOf<String, String>()
    campos.filter { it.requerido }.forEach { campo ->
        val valor = valores[campo.clave] ?: ""
        if (valor.isBlank()) {
            errores[campo.clave] = "Este campo es obligatorio"
        }
    }
    return errores
}