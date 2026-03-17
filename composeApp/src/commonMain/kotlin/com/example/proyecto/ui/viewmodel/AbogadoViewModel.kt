package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.User
import com.example.proyecto.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AbogadoUiState(
    val abogados: List<User> = emptyList(),
    val abogadosFiltrados: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val busqueda: String = "",
    val especialidadSeleccionada: String = "Todos"
)

class AbogadoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AbogadoUiState())
    val uiState: StateFlow<AbogadoUiState> = _uiState.asStateFlow()

    init {
        cargarAbogados()
    }

    fun cargarAbogados() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val lista = supabase.from("users")
                    .select {
                        filter { eq("tipo", "abogado") }
                        order("calificacion_promedio", Order.DESCENDING)
                    }
                    .decodeList<User>()

                _uiState.value = _uiState.value.copy(
                    abogados = lista,
                    abogadosFiltrados = lista,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar abogados"
                )
            }
        }
    }

    fun buscar(query: String) {
        _uiState.value = _uiState.value.copy(busqueda = query)
        aplicarFiltros()
    }

    fun filtrarPorEspecialidad(especialidad: String) {
        _uiState.value = _uiState.value.copy(especialidadSeleccionada = especialidad)
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val estado = _uiState.value
        var resultado = estado.abogados

        if (estado.especialidadSeleccionada != "Todos") {
            resultado = resultado.filter {
                it.especialidad?.contains(estado.especialidadSeleccionada, ignoreCase = true) == true
            }
        }
        if (estado.busqueda.isNotBlank()) {
            resultado = resultado.filter {
                it.nombre.contains(estado.busqueda, ignoreCase = true) ||
                it.especialidad?.contains(estado.busqueda, ignoreCase = true) == true
            }
        }
        _uiState.value = estado.copy(abogadosFiltrados = resultado)
    }
}
