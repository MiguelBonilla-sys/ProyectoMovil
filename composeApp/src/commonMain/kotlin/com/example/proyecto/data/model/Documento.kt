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

    // Campos de firma electrónica (FASE 0)
    @SerialName("proceso_firma_id")
    val procesoFirmaId: String? = null, // FK a procesos_firma.id

    @SerialName("sello_tiempo_verificado")
    val selloTiempoVerificado: String? = null, // Timestamp verificado de CAMERFIRMA

    @SerialName("tipo_firma")
    val tipoFirma: TipoFirma? = null, // SIMPLE o AVANZADA

    @SerialName("cadena_firmas")
    val cadenaFirmas: List<FirmaRegistro> = emptyList(), // Historial de firmas

    @SerialName("url_documento_firmado")
    val urlDocumentoFirmado: String? = null, // URL en Storage del PDF firmado

    @SerialName("codigo_verificacion")
    val codigoVerificacion: String? = null, // Para terceros verifiquen firma

    @SerialName("created_at")
    val createdAt: String? = null
)
