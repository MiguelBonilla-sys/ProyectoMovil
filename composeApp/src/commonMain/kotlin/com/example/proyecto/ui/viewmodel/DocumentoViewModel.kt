package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.Documento
import com.example.proyecto.data.model.EstadoFirma
import com.example.proyecto.data.model.TipoPlantilla
import com.example.proyecto.data.util.PdfGenerator
import com.example.proyecto.data.repository.DocumentoRepository
import com.example.proyecto.data.repository.DocumentoRepository.Companion.PAGE_SIZE
import com.example.proyecto.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocumentoUiState(
    val documentos: List<Documento> = emptyList(),
    val plantillas: List<Documento> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val filtroEstado: String = "Todos",
    val filtroTipo: String = "Todos",
    val busquedaTexto: String = "",
    val filtroFechaInicio: String? = null,
    val filtroFechaFin: String? = null,
    val ordenamiento: String = "fecha_desc",
    val currentPage: Int = 0,
    val hasMore: Boolean = true
)

class DocumentoViewModel(
    private val repository: DocumentoRepository = DocumentoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentoUiState())
    val uiState: StateFlow<DocumentoUiState> = _uiState.asStateFlow()

    init {
        cargarDocumentos()
    }

    fun cargarDocumentos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val userId = SessionManager.currentUser?.id ?: return@launch
                val docs = repository.obtenerPorUsuario(userId, limit = PAGE_SIZE, offset = 0)
                val plantillas = repository.obtenerPlantillas(limit = PAGE_SIZE, offset = 0)
                _uiState.value = _uiState.value.copy(
                    documentos = docs,
                    plantillas = plantillas,
                    isLoading = false,
                    currentPage = 0,
                    hasMore = docs.size >= PAGE_SIZE
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar documentos"
                )
            }
        }
    }

    fun buscarPorTexto(texto: String) {
        _uiState.value = _uiState.value.copy(busquedaTexto = texto)
        aplicarFiltros()
    }

    fun filtrarPorTipo(tipo: String) {
        _uiState.value = _uiState.value.copy(filtroTipo = tipo)
        aplicarFiltros()
    }

    fun filtrarPorRangoFechas(fechaInicio: String?, fechaFin: String?) {
        _uiState.value = _uiState.value.copy(
            filtroFechaInicio = fechaInicio,
            filtroFechaFin = fechaFin
        )
        aplicarFiltros()
    }

    fun ordenarPor(campo: String) {
        _uiState.value = _uiState.value.copy(ordenamiento = campo)
        aplicarFiltros()
    }

    fun limpiarFiltros() {
        _uiState.value = _uiState.value.copy(
            filtroEstado = "Todos",
            filtroTipo = "Todos",
            busquedaTexto = "",
            filtroFechaInicio = null,
            filtroFechaFin = null,
            ordenamiento = "fecha_desc"
        )
        cargarDocumentos()
    }

    private fun aplicarFiltros() {
        val state = _uiState.value
        var resultado = state.documentos

        if (state.busquedaTexto.isNotBlank()) {
            val texto = state.busquedaTexto.lowercase()
            resultado = resultado.filter {
                it.nombre.lowercase().contains(texto)
            }
        }

        when (state.filtroEstado) {
            "Pendientes" -> resultado = resultado.filter { it.estadoFirma == EstadoFirma.PENDIENTE }
            "Firmados"   -> resultado = resultado.filter { it.estadoFirma == EstadoFirma.FIRMADO }
            "Rechazados" -> resultado = resultado.filter { it.estadoFirma == EstadoFirma.RECHAZADO }
        }

        if (state.filtroTipo != "Todos") {
            resultado = resultado.filter { it.tipo == state.filtroTipo }
        }

        resultado = when (state.ordenamiento) {
            "nombre_asc" -> resultado.sortedBy { it.nombre }
            "nombre_desc" -> resultado.sortedByDescending { it.nombre }
            "fecha_asc" -> resultado.sortedBy { it.createdAt }
            else -> resultado.sortedByDescending { it.createdAt }
        }

        _uiState.value = state.copy(documentos = resultado)
    }

    fun filtrarPorEstado(filtro: String) {
        _uiState.value = _uiState.value.copy(filtroEstado = filtro)
        aplicarFiltros()
    }

    fun documentosFiltrados(): List<Documento> = _uiState.value.documentos

    fun cargarMas() {
        val state = _uiState.value
        if (!state.hasMore || state.isLoading || state.isLoadingMore) return
        val userId = SessionManager.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.value = state.copy(isLoadingMore = true)
            try {
                val nextPage = state.currentPage + 1
                val docs = repository.obtenerPorUsuario(userId, limit = PAGE_SIZE, offset = nextPage * PAGE_SIZE)
                _uiState.value = _uiState.value.copy(
                    documentos = state.documentos + docs,
                    isLoadingMore = false,
                    currentPage = nextPage,
                    hasMore = docs.size >= PAGE_SIZE
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false, error = e.message)
            }
        }
    }

    fun actualizarFirma(documentoId: String, estado: EstadoFirma, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.actualizarEstadoFirma(documentoId, estado)
                cargarDocumentos()
                onResult(true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
                onResult(false)
            }
        }
    }

    fun eliminarDocumento(documentoId: String, url: String) {
        viewModelScope.launch {
            try {
                repository.eliminarDocumento(documentoId, url)
                cargarDocumentos()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun crearDocumento(nombre: String, tipo: String, consultaId: String? = null, onResult: (Boolean, String?) -> Unit) {
        val userId = SessionManager.currentUser?.id ?: run { onResult(false, "Sesión no iniciada"); return }
        if (nombre.isBlank()) { onResult(false, "El nombre no puede estar vacío"); return }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.crearDesdeMetadata(nombre, tipo, userId, consultaId)
                cargarDocumentos()
                onResult(true, null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                onResult(false, e.message)
            }
        }
    }

    fun usarPlantilla(plantilla: Documento, onResult: (Boolean, String?) -> Unit) {
        val userId = SessionManager.currentUser?.id ?: run { onResult(false, "Sesión no iniciada"); return }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.crearDesdeMetadata(
                    nombre = plantilla.nombre,
                    tipo = plantilla.tipo,
                    subidoPor = userId,
                    url = plantilla.url
                )
                cargarDocumentos()
                onResult(true, null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                onResult(false, e.message)
            }
        }
    }

    fun generarPdfDesdePlantilla(
        tipo: TipoPlantilla,
        datos: Map<String, String>,
        nombreDocumento: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val userId = SessionManager.currentUser?.id ?: run { onResult(false, "Sesión no iniciada"); return }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val pdfBytes = PdfGenerator.generatePdf(tipo, datos, nombreDocumento)
                val documento = repository.subirDocumento(
                    nombre = "$nombreDocumento.pdf",
                    bytes = pdfBytes,
                    consultaId = null,
                    tipo = tipo.displayName,
                    subidoPor = userId,
                    esPlantilla = false
                )
                cargarDocumentos()
                onResult(true, null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                onResult(false, e.message)
            }
        }
    }

    fun subirArchivo(
        bytes: ByteArray,
        nombre: String,
        tipo: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val userId = SessionManager.currentUser?.id ?: run { onResult(false, "Sesión no iniciada"); return }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.subirDocumento(
                    nombre = nombre,
                    bytes = bytes,
                    consultaId = null,
                    tipo = tipo,
                    subidoPor = userId,
                    esPlantilla = false
                )
                cargarDocumentos()
                onResult(true, null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                onResult(false, e.message)
            }
        }
    }
}
