package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.Notificacion
import com.example.proyecto.data.remote.supabase
import com.example.proyecto.data.repository.NotificacionRepository
import com.example.proyecto.data.session.SessionManager
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class NotificacionUiState(
    val notificaciones: List<Notificacion> = emptyList(),
    val noLeidas: Int = 0,
    val isLoading: Boolean = false
)

class NotificacionViewModel(
    private val repository: NotificacionRepository = NotificacionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacionUiState())
    val uiState: StateFlow<NotificacionUiState> = _uiState.asStateFlow()

    private var realtimeChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null

    init {
        cargarNotificaciones()
        suscribirRealtime()
    }

    fun cargarNotificaciones() {
        viewModelScope.launch {
            val userId = SessionManager.currentUser?.id ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val lista = repository.obtenerPorUsuario(userId)
                _uiState.value = NotificacionUiState(
                    notificaciones = lista,
                    noLeidas = lista.count { !it.leida },
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun marcarLeida(notificacionId: String) {
        viewModelScope.launch {
            try {
                repository.marcarComoLeida(notificacionId)
                val actualizadas = _uiState.value.notificaciones.map {
                    if (it.id == notificacionId) it.copy(leida = true) else it
                }
                _uiState.value = _uiState.value.copy(
                    notificaciones = actualizadas,
                    noLeidas = actualizadas.count { !it.leida }
                )
            } catch (_: Exception) { }
        }
    }

    fun marcarTodasLeidas() {
        viewModelScope.launch {
            val userId = SessionManager.currentUser?.id ?: return@launch
            try {
                repository.marcarTodasLeidas(userId)
                val actualizadas = _uiState.value.notificaciones.map { it.copy(leida = true) }
                _uiState.value = _uiState.value.copy(notificaciones = actualizadas, noLeidas = 0)
            } catch (_: Exception) { }
        }
    }

    private fun suscribirRealtime() {
        val userId = SessionManager.currentUser?.id ?: return
        viewModelScope.launch {
            try {
                realtimeChannel = supabase.realtime.channel("notificaciones-$userId")
                val cambios = realtimeChannel!!.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "notificaciones"
                }
                cambios.onEach { action ->
                    // Recarga solo si la notificacion es para el usuario actual
                    cargarNotificaciones()
                }.launchIn(viewModelScope)
                realtimeChannel!!.subscribe()
            } catch (_: Exception) { }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            try { realtimeChannel?.unsubscribe() } catch (_: Exception) { }
        }
    }
}
