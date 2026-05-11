package com.example.proyecto.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Registro de auditoría para rastrear cambios en entidades críticas.
 * Fase 2: Auditoría y Seguridad
 */
@Serializable
data class AuditLog(
    @SerialName("id")
    val id: String? = null,

    @SerialName("usuario_id")
    val usuarioId: String? = null, // Quién hizo el cambio (nullable si es sistema)

    @SerialName("tabla_afectada")
    val tablaAfectada: String, // "consultas", "documentos", "notificaciones", "users"

    @SerialName("accion")
    val accion: String, // "INSERT", "UPDATE", "DELETE"

    @SerialName("registro_id")
    val registroId: String, // ID del registro modificado

    @SerialName("datos_anteriores")
    val datosAnteriores: JsonElement? = null, // JSONB como JsonElement

    @SerialName("datos_nuevos")
    val datosNuevos: JsonElement? = null, // JSONB como JsonElement

    @SerialName("ip_origen")
    val ipOrigen: String? = null,

    @SerialName("user_agent")
    val userAgent: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
) {
    /**
     * Descripción legible del cambio.
     */
    val descripcion: String
        get() {
            val accionDesc = accion.lowercase()
            return when (accion) {
                "INSERT" -> "Creó un registro en $tablaAfectada"
                "UPDATE" -> "Modificó un registro en $tablaAfectada"
                "DELETE" -> "Eliminó un registro de $tablaAfectada"
                else -> "Acción desconocida en $tablaAfectada"
            }
        }

    /**
     * Lista los campos que fueron modificados en el cambio.
     */
    val camposModificados: List<String>
        get() = when {
            accion == "INSERT" && datosNuevos != null -> datosNuevos.toString().split(",").take(5)
            accion == "DELETE" && datosAnteriores != null -> datosAnteriores.toString().split(",").take(5)
            accion == "UPDATE" && datosNuevos != null && datosAnteriores != null -> {
                datosNuevos.toString().split(",").take(5)
            }
            else -> emptyList()
        }
}
