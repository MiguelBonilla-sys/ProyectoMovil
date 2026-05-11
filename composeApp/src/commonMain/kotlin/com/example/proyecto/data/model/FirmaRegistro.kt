package com.example.proyecto.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representa un registro individual de firma en un documento.
 * Cada vez que alguien firma un documento, se crea un FirmaRegistro.
 */
@Serializable
data class FirmaRegistro(
    @SerialName("id")
    val id: String? = null,

    @SerialName("documento_id")
    val documentoId: String,

    @SerialName("proceso_firma_id")
    val procesoFirmaId: String,

    @SerialName("firmante_id")
    val firmanteId: String,

    @SerialName("firma_data")
    val firmaData: String, // Base64 de la firma digital

    @SerialName("sello_tiempo")
    val selloTiempo: String, // ISO 8601 timestamp de CAMERFIRMA

    @SerialName("ip_firmante")
    val ipFirmante: String,

    @SerialName("user_agent")
    val userAgent: String?,

    @SerialName("certificado_id")
    val certificadoId: String?, // ID del certificado usado

    @SerialName("algoritmo_firma")
    val algoritmoFirma: String = "SHA256withRSA", // Algoritmo usado

    @SerialName("estado")
    val estado: String = "FIRMADO", // FIRMADO, RECHAZADO

    @SerialName("motivo_rechazo")
    val motivoRechazo: String? = null, // Si fue rechazado, motivo

    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * Enum para los tipos de firma soportados.
 */
@Serializable
enum class TipoFirma {
    @SerialName("simple")
    SIMPLE, // Firma electrónica simple (según Ley 527)

    @SerialName("avanzada")
    AVANZADA // Firma electrónica avanzada con certificado (según Decreto 2364)
}
