package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.Consulta
import com.example.proyecto.data.model.ConsultaAbogado
import com.example.proyecto.data.model.EstadoConsulta
import com.example.proyecto.data.repository.ConsultaRepository
import com.example.proyecto.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConsultaUiState(
    val consultas: List<Consulta> = emptyList(),
    val consultasFiltradas: List<Consulta> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val estadoFiltro: String = "Todas",
    val areaFiltro: String = "Todas"
)

class ConsultaViewModel(
    private val repository: ConsultaRepository = ConsultaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsultaUiState())
    val uiState: StateFlow<ConsultaUiState> = _uiState.asStateFlow()

    init {
        cargarConsultas()
    }

    fun cargarConsultas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val userId = SessionManager.currentUser?.id ?: return@launch
                val lista = repository.obtenerPorCliente(userId)
                _uiState.value = _uiState.value.copy(
                    consultas = lista,
                    consultasFiltradas = lista,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar consultas"
                )
            }
        }
    }

    fun crearConsulta(
        areaPractica: String,
        descripcion: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = SessionManager.currentUser?.id ?: run { onError("Sesión no iniciada"); return }
        viewModelScope.launch {
            try {
                val consulta = Consulta(
                    clienteId = userId,
                    areaPractica = areaPractica,
                    descripcion = descripcion
                )
                repository.crearConsulta(consulta)
                cargarConsultas()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al crear consulta")
            }
        }
    }

    fun actualizarEstado(consultaId: String, nuevoEstado: EstadoConsulta) {
        viewModelScope.launch {
            try {
                repository.actualizarEstado(consultaId, nuevoEstado)
                cargarConsultas()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun asignarAbogado(consultaId: String, abogadoId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.asignarAbogado(consultaId, abogadoId)
                cargarConsultas()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al asignar abogado")
            }
        }
    }

    fun filtrarPorEstado(estado: String) {
        _uiState.value = _uiState.value.copy(estadoFiltro = estado)
        aplicarFiltros()
    }

    fun filtrarPorArea(area: String) {
        _uiState.value = _uiState.value.copy(areaFiltro = area)
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val estado = _uiState.value
        var resultado = estado.consultas
        if (estado.estadoFiltro != "Todas") {
            val filtroEstado = when (estado.estadoFiltro) {
                "Activas" -> EstadoConsulta.EN_CURSO
                "Abiertas" -> EstadoConsulta.ABIERTA
                "Cerradas" -> EstadoConsulta.CERRADA
                else -> null
            }
            if (filtroEstado != null) resultado = resultado.filter { it.estado == filtroEstado }
        }
        if (estado.areaFiltro != "Todas") {
            resultado = resultado.filter { it.areaPractica.contains(estado.areaFiltro, ignoreCase = true) }
        }
        _uiState.value = estado.copy(consultasFiltradas = resultado)
    }
}
