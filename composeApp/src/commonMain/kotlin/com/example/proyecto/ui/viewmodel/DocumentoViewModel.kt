package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.Documento
import com.example.proyecto.data.model.EstadoFirma
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

    fun usarPlantilla(plantilla: com.example.proyecto.data.model.Documento, onResult: (Boolean, String?) -> Unit) {
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

    fun filtrarPorEstado(filtro: String) {
        _uiState.value = _uiState.value.copy(filtroEstado = filtro)
    }

    fun documentosFiltrados(): List<Documento> {
        val docs = _uiState.value.documentos
        return when (_uiState.value.filtroEstado) {
            "Pendientes" -> docs.filter { it.estadoFirma == EstadoFirma.PENDIENTE }
            "Firmados"   -> docs.filter { it.estadoFirma == EstadoFirma.FIRMADO }
            "Rechazados" -> docs.filter { it.estadoFirma == EstadoFirma.RECHAZADO }
            else         -> docs
        }
    }
}
