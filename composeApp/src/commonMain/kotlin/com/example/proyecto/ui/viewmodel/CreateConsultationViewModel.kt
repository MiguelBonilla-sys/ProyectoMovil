package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.Consulta
import com.example.proyecto.data.repository.ConsultaRepository
import com.example.proyecto.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateConsultaUiState(
    val areaPractica: String = "",
    val descripcion: String = "",
    val abogadoId: String? = null,
    val abogadoNombre: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class CreateConsultationViewModel(
    private val repository: ConsultaRepository = ConsultaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateConsultaUiState())
    val uiState: StateFlow<CreateConsultaUiState> = _uiState.asStateFlow()

    fun setAbogado(id: String?, nombre: String?) {
        _uiState.value = _uiState.value.copy(abogadoId = id, abogadoNombre = nombre)
    }

    fun setArea(area: String) {
        _uiState.value = _uiState.value.copy(areaPractica = area)
    }

    fun setDescripcion(desc: String) {
        _uiState.value = _uiState.value.copy(descripcion = desc)
    }

    fun crear(onSuccess: () -> Unit) {
        val clienteId = SessionManager.currentUser?.id
        if (clienteId == null) {
            _uiState.value = _uiState.value.copy(error = "Sesión no iniciada")
            return
        }
        val state = _uiState.value
        if (state.areaPractica.isBlank()) {
            _uiState.value = state.copy(error = "Selecciona un área de práctica")
            return
        }
        if (state.descripcion.isBlank()) {
            _uiState.value = state.copy(error = "La descripción no puede estar vacía")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val consulta = Consulta(
                    clienteId = clienteId,
                    areaPractica = state.areaPractica,
                    descripcion = state.descripcion
                )
                val creada = repository.crearConsulta(consulta)
                val abogadoId = state.abogadoId
                if (!abogadoId.isNullOrBlank() && creada.id != null) {
                    repository.asignarAbogado(creada.id, abogadoId)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al crear la consulta"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
