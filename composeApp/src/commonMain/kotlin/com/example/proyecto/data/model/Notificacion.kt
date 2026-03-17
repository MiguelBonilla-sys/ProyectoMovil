package com.example.proyecto.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TipoNotificacion {
    @SerialName("consulta_nueva")     CONSULTA_NUEVA,
    @SerialName("documento_firmado")  DOCUMENTO_FIRMADO,
    @SerialName("estado_cambio")      ESTADO_CAMBIO,
    @SerialName("sistema")            SISTEMA
}

@Serializable
data class Notificacion(
    @SerialName("id")
    val id: String? = null,

    @SerialName("usuario_id")
    val usuarioId: String,

    @SerialName("tipo")
    val tipo: TipoNotificacion = TipoNotificacion.SISTEMA,

    @SerialName("titulo")
    val titulo: String,

    @SerialName("mensaje")
    val mensaje: String,

    @SerialName("leida")
    val leida: Boolean = false,

    @SerialName("created_at")
    val createdAt: String? = null
)
