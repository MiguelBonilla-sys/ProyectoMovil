package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.data.repository.ReporteConsultasAbogado
import com.example.proyecto.data.repository.ReporteRepository
import com.example.proyecto.data.repository.ReporteResumenGeneral
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportesUiState(
    val resumen: ReporteResumenGeneral? = null,
    val consultasPorAbogado: List<ReporteConsultasAbogado> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReportesViewModel(
    private val repository: ReporteRepository = ReporteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportesUiState())
    val uiState: StateFlow<ReportesUiState> = _uiState.asStateFlow()

    init { cargarReportes() }

    fun cargarReportes() {
        viewModelScope.launch {
            _uiState.value = ReportesUiState(isLoading = true)
            try {
                val resumen = repository.obtenerResumenGeneral()
                val porAbogado = repository.obtenerConsultasPorAbogado()
                _uiState.value = ReportesUiState(
                    resumen = resumen,
                    consultasPorAbogado = porAbogado,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = ReportesUiState(
                    isLoading = false,
                    error = e.message ?: "Error al cargar reportes"
                )
            }
        }
    }
}

@Composable
fun ReportesScreen(reportesViewModel: ReportesViewModel = viewModel()) {
    val uiState by reportesViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Reportes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Solo administradores",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF00BFA5)
                    )
                }
                IconButton(onClick = { reportesViewModel.cargarReportes() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Recargar", tint = Color.White)
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
            else -> {
                uiState.resumen?.let { resumen ->
                    item {
                        Text(
                            "Resumen General",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricCard("Clientes", resumen.totalClientes.toString(), Color(0xFF3949AB), Modifier.weight(1f))
                            MetricCard("Abogados", resumen.totalAbogados.toString(), Color(0xFF00BFA5), Modifier.weight(1f))
                            MetricCard("Consultas", resumen.totalConsultas.toString(), Color(0xFFFFA726), Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricCard("Abiertas", resumen.consultasAbiertas.toString(), Color(0xFF5C6BC0), Modifier.weight(1f))
                            MetricCard("Documentos", resumen.totalDocumentos.toString(), Color(0xFF26C6DA), Modifier.weight(1f))
                            MetricCard("Plantillas", resumen.totalPlantillas.toString(), Color(0xFF66BB6A), Modifier.weight(1f))
                        }
                    }
                }

                if (uiState.consultasPorAbogado.isNotEmpty()) {
                    item {
                        Text(
                            "Consultas por Abogado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    items(uiState.consultasPorAbogado) { reporte ->
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
                                    Icons.Filled.Analytics,
                                    contentDescription = null,
                                    tint = Color(0xFF3949AB),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        reporte.abogadoNombre,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    reporte.especialidad?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFF3949AB))
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${reporte.totalConsultas}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "consultas",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun MetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        }
    }
}
