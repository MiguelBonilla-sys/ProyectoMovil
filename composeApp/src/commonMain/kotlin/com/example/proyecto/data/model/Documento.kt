package com.example.proyecto.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EstadoFirma(val display: String) {
    @SerialName("pendiente")  PENDIENTE("Pendiente"),
    @SerialName("firmado")    FIRMADO("Firmado"),
    @SerialName("rechazado")  RECHAZADO("Rechazado")
}

@Serializable
data class Documento(
    @SerialName("id")
    val id: String? = null,

    @SerialName("consulta_id")
    val consultaId: String? = null,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("url")
    val url: String,

    @SerialName("tipo")
    val tipo: String,

    @SerialName("es_plantilla")
    val esPlantilla: Boolean = false,

    @SerialName("estado_firma")
    val estadoFirma: EstadoFirma = EstadoFirma.PENDIENTE,

    @SerialName("subido_por")
    val subidoPor: String? = null,

    @SerialName("ip_subida")
    val ipSubida: String? = null,

    @SerialName("sello_tiempo")
    val selloTiempo: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)
