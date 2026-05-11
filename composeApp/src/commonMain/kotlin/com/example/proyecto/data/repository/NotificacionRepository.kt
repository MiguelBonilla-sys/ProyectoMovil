package com.example.proyecto.data.repository

import com.example.proyecto.data.model.Notificacion
import com.example.proyecto.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class NotificacionRepository {

    suspend fun obtenerPorUsuario(usuarioId: String): List<Notificacion> =
        supabase.from("notificaciones")
            .select {
                filter { eq("usuario_id", usuarioId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Notificacion>()

    suspend fun marcarComoLeida(notificacionId: String) {
        supabase.from("notificaciones")
            .update({ set("leida", true) }) {
                filter { eq("id", notificacionId) }
            }
    }

    suspend fun marcarTodasLeidas(usuarioId: String) {
        supabase.from("notificaciones")
            .update({ set("leida", true) }) {
                filter {
                    eq("usuario_id", usuarioId)
                    eq("leida", false)
                }
            }
    }

    suspend fun contarNoLeidas(usuarioId: String): Int =
        supabase.from("notificaciones")
            .select {
                filter {
                    eq("usuario_id", usuarioId)
                    eq("leida", false)
                }
            }
            .decodeList<Notificacion>()
            .size

    suspend fun eliminarNotificacion(notificacionId: String) {
        supabase.from("notificaciones")
            .delete {
                filter { eq("id", notificacionId) }
            }
    }
}
