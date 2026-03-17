package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.Documento
import com.example.proyecto.data.model.EstadoFirma
import com.example.proyecto.data.repository.DocumentoRepository
import com.example.proyecto.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocumentoUiState(
    val documentos: List<Documento> = emptyList(),
    val plantillas: List<Documento> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filtroEstado: String = "Todos"
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
                val docs = repository.obtenerPorUsuario(userId)
                val plantillas = repository.obtenerPlantillas()
                _uiState.value = _uiState.value.copy(
                    documentos = docs,
                    plantillas = plantillas,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar documentos"
                )
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
