package com.example.proyecto.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum para los estados posibles de un proceso de firma en CAMERFIRMA.
 */
@Serializable
enum class EstadoProcesoFirma {
    @SerialName("iniciado")
    INICIADO, // Proceso recién creado en CAMERFIRMA

    @SerialName("en_progreso")
    EN_PROGRESO, // Esperando firmas

    @SerialName("completo")
    COMPLETO, // Todas las firmas recolectadas

    @SerialName("expirado")
    EXPIRADO, // Proceso expiró (ej: 30 días)

    @SerialName("cancelado")
    CANCELADO, // Usuario o admin canceló

    @SerialName("error")
    ERROR // Error en CAMERFIRMA
}

/**
 * Representa un proceso de firma en CAMERFIRMA.
 * Es la entidad que conecta un documento con CAMERFIRMA.
 */
@Serializable
data class ProcesoFirma(
    @SerialName("id")
    val id: String, // UUID generado localmente o en CAMERFIRMA

    @SerialName("documento_id")
    val documentoId: String, // FK a documentos.id

    @SerialName("camerfirma_id")
    val camerfirmaId: String?, // ID del proceso en CAMERFIRMA

    @SerialName("camerfirma_url")
    val camerfirmaUrl: String?, // URL para que usuario firme en CAMERFIRMA

    @SerialName("estado")
    val estado: EstadoProcesoFirma = EstadoProcesoFirma.INICIADO,

    @SerialName("tipo_firma")
    val tipoFirma: TipoFirma = TipoFirma.SIMPLE,

    @SerialName("firmantes_requeridos")
    val firmantesRequeridos: List<String>, // IDs de usuarios que deben firmar

    @SerialName("firmas_completadas")
    val firmasCompletadas: List<FirmaRegistro> = emptyList(),

    @SerialName("url_documento_firmado")
    val urlDocumentoFirmado: String?, // URL en Supabase Storage del PDF firmado

    @SerialName("codigo_verificacion")
    val codigoVerificacion: String?, // Código para que terceros verifiquen la firma

    @SerialName("fecha_vencimiento")
    val fechaVencimiento: String?, // ISO 8601 - cuándo expira el proceso

    @SerialName("intentos_fallidos")
    val intentosFallidos: Int = 0,

    @SerialName("errores")
    val errores: List<String> = emptyList(),

    @SerialName("initiated_by")
    val initiadoPor: String, // ID del usuario que inició el proceso

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("completed_at")
    val completadoEn: String? = null // ISO 8601 - cuándo se completó
) {
    /**
     * ¿El proceso está completo (todas las firmas recolectadas)?
     */
    val estaCompleto: Boolean
        get() = firmasCompletadas.size == firmantesRequeridos.size

    /**
     * ¿El usuario dado ya firmó?
     */
    fun yaFirmo(usuarioId: String): Boolean =
        firmasCompletadas.any { it.firmanteId == usuarioId && it.estado == "FIRMADO" }

    /**
     * Porcentaje de firmas completadas
     */
    val porcentajeCompletitud: Int
        get() = if (firmantesRequeridos.isEmpty()) 0 else (firmasCompletadas.size * 100) / firmantesRequeridos.size
}
