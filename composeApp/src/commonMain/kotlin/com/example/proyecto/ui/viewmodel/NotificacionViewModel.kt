package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.Notificacion
import com.example.proyecto.data.model.TipoNotificacion
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Estado UI para la pantalla de notificaciones.
 * Fase 2: Integración Realtime (actualizado para mejor performance)
 */
data class NotificacionUiState(
    val notificaciones: List<Notificacion> = emptyList(),
    val noLeidas: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val sincronizando: Boolean = false
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

    /**
     * Carga todas las notificaciones del usuario desde BD.
     */
    fun cargarNotificaciones() {
        viewModelScope.launch {
            val userId = SessionManager.currentUser?.id ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val lista = repository.obtenerPorUsuario(userId)
                _uiState.value = NotificacionUiState(
                    notificaciones = lista,
                    noLeidas = lista.count { !it.leida },
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar notificaciones"
                )
            }
        }
    }

    /**
     * Marca una notificación como leída.
     * Actualiza el estado UI inmediatamente y sincroniza con BD.
     */
    fun marcarLeida(notificacionId: String) {
        viewModelScope.launch {
            try {
                // Actualizar UI inmediatamente
                val actualizadas = _uiState.value.notificaciones.map {
                    if (it.id == notificacionId) it.copy(leida = true) else it
                }
                _uiState.value = _uiState.value.copy(
                    notificaciones = actualizadas,
                    noLeidas = actualizadas.count { !it.leida }
                )

                // Sincronizar con BD en background
                repository.marcarComoLeida(notificacionId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al marcar notificación"
                )
            }
        }
    }

    /**
     * Marca todas las notificaciones como leídas.
     */
    fun marcarTodasLeidas() {
        viewModelScope.launch {
            val userId = SessionManager.currentUser?.id ?: return@launch
            try {
                _uiState.value = _uiState.value.copy(sincronizando = true)

                // Actualizar UI inmediatamente
                val actualizadas = _uiState.value.notificaciones.map { it.copy(leida = true) }
                _uiState.value = _uiState.value.copy(
                    notificaciones = actualizadas,
                    noLeidas = 0,
                    sincronizando = false
                )

                // Sincronizar con BD
                repository.marcarTodasLeidas(userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al marcar todas",
                    sincronizando = false
                )
            }
        }
    }

    /**
     * Elimina una notificación.
     */
    fun eliminarNotificacion(notificacionId: String) {
        viewModelScope.launch {
            try {
                // Actualizar UI inmediatamente
                val actualizadas = _uiState.value.notificaciones.filter { it.id != notificacionId }
                _uiState.value = _uiState.value.copy(
                    notificaciones = actualizadas,
                    noLeidas = actualizadas.count { !it.leida }
                )

                // Eliminar de BD
                repository.eliminarNotificacion(notificacionId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al eliminar"
                )
            }
        }
    }

    /**
     * Suscribe a cambios en tiempo real de notificaciones.
     * Utiliza Realtime de Supabase para recibir actualizaciones en vivo.
     */
    private fun suscribirRealtime() {
        val userId = SessionManager.currentUser?.id ?: return
        viewModelScope.launch {
            try {
                // Crear canal para escuchar cambios en la tabla notificaciones
                realtimeChannel = supabase.realtime.channel("notificaciones-$userId")

                // Escuchar INSERT (notificaciones nuevas)
                realtimeChannel!!
                    .postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                        table = "notificaciones"
                    }
                    .onEach { action ->
                        manejarNuevaNotificacion(action)
                    }
                    .launchIn(viewModelScope)

                // Escuchar UPDATE (cambios en notificaciones existentes)
                realtimeChannel!!
                    .postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                        table = "notificaciones"
                    }
                    .onEach { action ->
                        manejarActualizacionNotificacion(action)
                    }
                    .launchIn(viewModelScope)

                // Escuchar DELETE (eliminación de notificaciones)
                realtimeChannel!!
                    .postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
                        table = "notificaciones"
                    }
                    .onEach { action ->
                        manejarEliminacionNotificacion(action)
                    }
                    .launchIn(viewModelScope)

                // Suscribirse al canal
                realtimeChannel!!.subscribe()
            } catch (e: Exception) {
                // Log silencioso de error de Realtime
                _uiState.value = _uiState.value.copy(
                    error = "Notificaciones en tiempo real desconectadas"
                )
            }
        }
    }

    /**
     * Maneja la inserción de una nueva notificación en tiempo real.
     */
    private fun manejarNuevaNotificacion(action: PostgresAction.Insert) {
        try {
            val nuevosData = action.record
            val usuarioIdData = nuevosData["usuario_id"] as? String ?: return

            // Solo agregar si es para el usuario actual
            if (usuarioIdData == SessionManager.currentUser?.id) {
                val mensaje = nuevosData["mensaje"] as? String ?: ""
                val leida = (nuevosData["leida"] as? Boolean) ?: false
                val id = nuevosData["id"] as? String ?: return

                val nuevaNotificacion = Notificacion(
                    id = id,
                    usuarioId = usuarioIdData,
                    tipo = parsearTipoNotificacion(nuevosData["tipo"] as? String),
                    titulo = nuevosData["titulo"] as? String ?: "Nueva notificación",
                    mensaje = mensaje,
                    leida = leida,
                    createdAt = (nuevosData["created_at"] as? String)
                )

                // Agregar a la lista (nueva notificación al inicio)
                val actualizadas = listOf(nuevaNotificacion) + _uiState.value.notificaciones
                _uiState.value = _uiState.value.copy(
                    notificaciones = actualizadas,
                    noLeidas = actualizadas.count { !it.leida }
                )
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }

    /**
     * Maneja la actualización de una notificación en tiempo real.
     */
    private fun manejarActualizacionNotificacion(action: PostgresAction.Update) {
        try {
            val datosNuevos = action.record
            val id = datosNuevos["id"] as? String ?: return
            val leida = (datosNuevos["leida"] as? Boolean) ?: false

            val actualizadas = _uiState.value.notificaciones.map { notif ->
                if (notif.id == id) {
                    notif.copy(leida = leida)
                } else {
                    notif
                }
            }

            _uiState.value = _uiState.value.copy(
                notificaciones = actualizadas,
                noLeidas = actualizadas.count { !it.leida }
            )
        } catch (e: Exception) {
            // Silent fail
        }
    }

    /**
     * Maneja la eliminación de una notificación en tiempo real.
     */
    private fun manejarEliminacionNotificacion(action: PostgresAction.Delete) {
        try {
            val datosAnteriores = action.oldRecord
            val id = datosAnteriores["id"] as? String ?: return

            val actualizadas = _uiState.value.notificaciones.filter { it.id != id }
            _uiState.value = _uiState.value.copy(
                notificaciones = actualizadas,
                noLeidas = actualizadas.count { !it.leida }
            )
        } catch (e: Exception) {
            // Silent fail
        }
    }

    /**
     * Limpia mensajes de error.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Parsea tipo de notificación desde String a TipoNotificacion enum.
     */
    private fun parsearTipoNotificacion(tipo: String?): TipoNotificacion {
        return when (tipo) {
            "consulta_nueva" -> TipoNotificacion.CONSULTA_NUEVA
            "documento_firmado" -> TipoNotificacion.DOCUMENTO_FIRMADO
            "estado_cambio" -> TipoNotificacion.ESTADO_CAMBIO
            else -> TipoNotificacion.SISTEMA
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            try {
                realtimeChannel?.unsubscribe()
            } catch (_: Exception) {
                // Silent fail
            }
        }
    }
}
