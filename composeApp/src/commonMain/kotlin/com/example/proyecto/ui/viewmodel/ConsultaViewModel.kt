package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.Consulta
import com.example.proyecto.data.model.ConsultaAbogado
import com.example.proyecto.data.model.EstadoConsulta
import com.example.proyecto.data.repository.ConsultaRepository
import com.example.proyecto.data.repository.ConsultaRepository.Companion.PAGE_SIZE
import com.example.proyecto.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConsultaUiState(
    val consultas: List<Consulta> = emptyList(),
    val consultasFiltradas: List<Consulta> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val estadoFiltro: String = "Todas",
    val areaFiltro: String = "Todas",
    val abogadoFiltro: String? = null,
    val busquedaTexto: String = "",
    val filtroFechaInicio: String? = null,
    val filtroFechaFin: String? = null,
    val ordenamiento: String = "fecha_desc",
    val areasDisponibles: List<String> = listOf(
        "Derecho Civil", "Derecho Penal", "Derecho Laboral", "Derecho Familiar",
        "Derecho Comercial", "Derecho Administrativo", "Derecho Internacional"
    ),
    val currentPage: Int = 0,
    val hasMore: Boolean = true
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
                val lista = repository.obtenerPorCliente(userId, limit = PAGE_SIZE, offset = 0)
                _uiState.value = _uiState.value.copy(
                    consultas = lista,
                    consultasFiltradas = lista,
                    isLoading = false,
                    currentPage = 0,
                    hasMore = lista.size >= PAGE_SIZE
                )
                aplicarFiltros()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar consultas"
                )
            }
        }
    }

    fun buscarPorTexto(texto: String) {
        _uiState.value = _uiState.value.copy(busquedaTexto = texto)
        aplicarFiltros()
    }

    fun filtrarPorRangoFechas(fechaInicio: String?, fechaFin: String?) {
        _uiState.value = _uiState.value.copy(
            filtroFechaInicio = fechaInicio,
            filtroFechaFin = fechaFin
        )
        aplicarFiltros()
    }

    fun filtrarPorAbogado(abogadoId: String?) {
        _uiState.value = _uiState.value.copy(abogadoFiltro = abogadoId)
        aplicarFiltros()
    }

    fun ordenarPor(campo: String) {
        _uiState.value = _uiState.value.copy(ordenamiento = campo)
        aplicarFiltros()
    }

    fun limpiarFiltros() {
        _uiState.value = _uiState.value.copy(
            estadoFiltro = "Todas",
            areaFiltro = "Todas",
            abogadoFiltro = null,
            busquedaTexto = "",
            filtroFechaInicio = null,
            filtroFechaFin = null,
            ordenamiento = "fecha_desc"
        )
        cargarConsultas()
    }

    private fun aplicarFiltros() {
        val estado = _uiState.value
        var resultado = estado.consultas

        if (estado.busquedaTexto.isNotBlank()) {
            val texto = estado.busquedaTexto.lowercase()
            resultado = resultado.filter {
                it.descripcion.lowercase().contains(texto) ||
                it.areaPractica.lowercase().contains(texto)
            }
        }

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

        resultado = when (estado.ordenamiento) {
            "fecha_asc" -> resultado.sortedBy { it.createdAt }
            else -> resultado.sortedByDescending { it.createdAt }
        }

        _uiState.value = estado.copy(consultasFiltradas = resultado)
    }

    fun cargarMas() {
        val state = _uiState.value
        if (!state.hasMore || state.isLoading || state.isLoadingMore) return
        val userId = SessionManager.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.value = state.copy(isLoadingMore = true)
            try {
                val nextPage = state.currentPage + 1
                val lista = repository.obtenerPorCliente(userId, limit = PAGE_SIZE, offset = nextPage * PAGE_SIZE)
                val todas = state.consultas + lista
                _uiState.value = _uiState.value.copy(
                    consultas = todas,
                    isLoadingMore = false,
                    currentPage = nextPage,
                    hasMore = lista.size >= PAGE_SIZE
                )
                aplicarFiltros()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false, error = e.message)
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

    fun eliminarConsulta(consultaId: String) {
        viewModelScope.launch {
            try {
                repository.eliminarConsulta(consultaId)
                cargarConsultas()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun filtrarPorArea(area: String) {
        _uiState.value = _uiState.value.copy(areaFiltro = area)
        aplicarFiltros()
    }
}
