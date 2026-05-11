package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.AuditLog
import com.example.proyecto.data.repository.AuditLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Estado UI para la pantalla de auditoría.
 * Fase 2: Auditoría y Seguridad
 */
data class AuditLogUiState(
    val logs: List<AuditLog> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,

    // Filtros
    val tablaSeleccionada: String? = null,
    val accionSeleccionada: String? = null,
    val fechaInicio: LocalDate? = null,
    val fechaFin: LocalDate? = null,

    // Paginación
    val currentPage: Int = 0,
    val totalPages: Int = 0,

    // Resumen
    val resumen: Map<String, Map<String, Int>> = emptyMap(),

    // Para export
    val isExporting: Boolean = false,
    val csvExportado: String? = null,

    // Estado de filtros
    val filtrosActivos: Boolean = false
)

class AuditLogViewModel(
    private val repository: AuditLogRepository = AuditLogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditLogUiState())
    val uiState: StateFlow<AuditLogUiState> = _uiState.asStateFlow()

    companion object {
        const val PAGE_SIZE = 50
    }

    init {
        cargarLogs()
        cargarResumen()
    }

    /**
     * Carga la página actual de logs con filtros aplicados.
     */
    private fun cargarLogs(page: Int = 0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val logs = repository.buscar(
                    tabla = _uiState.value.tablaSeleccionada,
                    accion = _uiState.value.accionSeleccionada,
                    fechaInicio = _uiState.value.fechaInicio?.toString(),
                    fechaFin = _uiState.value.fechaFin?.toString(),
                    limit = PAGE_SIZE,
                    offset = page * PAGE_SIZE
                )

                val totalPages = if (logs.isEmpty()) 1 else (logs.size / PAGE_SIZE) + 1

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    logs = logs,
                    currentPage = page,
                    totalPages = totalPages
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar logs"
                )
            }
        }
    }

    /**
     * Carga el resumen de actividades por tabla.
     */
    private fun cargarResumen() {
        viewModelScope.launch {
            try {
                val resumen = repository.obtenerResumen()
                _uiState.value = _uiState.value.copy(resumen = resumen)
            } catch (e: Exception) {
                // Silent fail para resumen
            }
        }
    }

    /**
     * Filtra logs por tabla.
     */
    fun filtrarPorTabla(tabla: String?) {
        _uiState.value = _uiState.value.copy(
            tablaSeleccionada = tabla,
            filtrosActivos = true,
            currentPage = 0
        )
        cargarLogs()
    }

    /**
     * Filtra logs por acción (INSERT, UPDATE, DELETE).
     */
    fun filtrarPorAccion(accion: String?) {
        _uiState.value = _uiState.value.copy(
            accionSeleccionada = accion,
            filtrosActivos = true,
            currentPage = 0
        )
        cargarLogs()
    }

    /**
     * Filtra logs por rango de fechas.
     */
    fun filtrarPorFechas(fechaInicio: LocalDate?, fechaFin: LocalDate?) {
        _uiState.value = _uiState.value.copy(
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            filtrosActivos = true,
            currentPage = 0
        )
        cargarLogs()
    }

    /**
     * Limpia todos los filtros.
     */
    fun limpiarFiltros() {
        _uiState.value = _uiState.value.copy(
            tablaSeleccionada = null,
            accionSeleccionada = null,
            fechaInicio = null,
            fechaFin = null,
            filtrosActivos = false,
            currentPage = 0
        )
        cargarLogs()
    }

    /**
     * Va a la página anterior.
     */
    fun paginaAnterior() {
        if (_uiState.value.currentPage > 0) {
            cargarLogs(_uiState.value.currentPage - 1)
        }
    }

    /**
     * Va a la página siguiente.
     */
    fun paginaSiguiente() {
        if (_uiState.value.currentPage < _uiState.value.totalPages - 1) {
            cargarLogs(_uiState.value.currentPage + 1)
        }
    }

    /**
     * Carga actividades recientes (últimas 24 horas).
     */
    fun cargarActividadesRecientes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val logs = repository.obtenerActividadesRecientes(limite = PAGE_SIZE)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    logs = logs,
                    filtrosActivos = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar actividades recientes"
                )
            }
        }
    }

    /**
     * Obtiene historial de cambios de un documento específico.
     */
    fun obtenerHistorialDocumento(documentoId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val logs = repository.obtenerHistorialDocumento(documentoId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    logs = logs
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar historial"
                )
            }
        }
    }

    /**
     * Exporta logs como CSV.
     */
    fun exportarCSV() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, error = null)
            try {
                val csv = repository.exportarCSV(
                    tabla = _uiState.value.tablaSeleccionada,
                    fechaInicio = _uiState.value.fechaInicio?.toString(),
                    fechaFin = _uiState.value.fechaFin?.toString()
                )

                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    csvExportado = csv,
                    successMessage = "CSV exportado exitosamente"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = e.message ?: "Error al exportar CSV"
                )
            }
        }
    }

    /**
     * Limpia mensajes.
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    /**
     * Obtiene el total de cambios por acción.
     */
    fun obtenerTotalPorAccion(): Map<String, Int> {
        val resumen = _uiState.value.resumen
        val totales = mutableMapOf<String, Int>()

        resumen.values.forEach { accionMap ->
            accionMap.forEach { (accion, count) ->
                totales[accion] = (totales[accion] ?: 0) + count
            }
        }

        return totales
    }
}
