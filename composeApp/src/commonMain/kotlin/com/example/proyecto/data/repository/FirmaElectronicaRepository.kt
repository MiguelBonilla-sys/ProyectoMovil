package com.example.proyecto.data.repository

import com.example.proyecto.data.model.EstadoProcesoFirma
import com.example.proyecto.data.model.FirmaRegistro
import com.example.proyecto.data.model.ProcesoFirma
import com.example.proyecto.data.model.TipoFirma
import com.example.proyecto.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class FirmaElectronicaRepository {
    private val supabase = SupabaseClientProvider.client
    private val postgrestClient = supabase.postgrest

    companion object {
        const val TABLA_PROCESOS_FIRMA = "procesos_firma"
        const val TABLA_FIRMAS_REGISTROS = "firmas_registros"
        const val VENCIMIENTO_PROCESO_DIAS = 30

        fun generarId(): String {
            return "${Random.nextLong().toString(36)}-${Random.nextLong().toString(36)}"
        }

        fun generarCodigoVerificacion(): String {
            return (1..8).map { ('0'..'9').random() }.joinToString("")
        }
    }

    suspend fun iniciarProcesoFirma(
        documentoId: String,
        firmantesIds: List<String>,
        tipoFirma: TipoFirma = TipoFirma.SIMPLE,
        usuarioActualId: String
    ): ProcesoFirma {
        require(firmantesIds.isNotEmpty()) { "Debe haber al menos un firmante" }

        val procesoId = generarId()
        val ahora = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME)
        val vencimiento = LocalDateTime.now(ZoneOffset.UTC).plusDays(VENCIMIENTO_PROCESO_DIAS.toLong())
            .format(DateTimeFormatter.ISO_DATE_TIME)

        val nuevoProc = ProcesoFirma(
            id = procesoId,
            documentoId = documentoId,
            camerfirmaId = procesoId,
            camerfirmaUrl = "FIRMA_PENDIENTE",
            estado = EstadoProcesoFirma.INICIADO,
            tipoFirma = tipoFirma,
            firmantesRequeridos = firmantesIds,
            initiadoPor = usuarioActualId,
            fechaVencimiento = vencimiento,
            urlDocumentoFirmado = null,
            codigoVerificacion = generarCodigoVerificacion(),
            createdAt = ahora
        )

        postgrestClient
            .from(TABLA_PROCESOS_FIRMA)
            .insert(nuevoProc)

        return nuevoProc
    }

    suspend fun verificarEstadoFirma(procesoId: String): ProcesoFirma? {
        return try {
            postgrestClient
                .from(TABLA_PROCESOS_FIRMA)
                .select { filter { eq("id", procesoId) } }
                .decodeSingleOrNull<ProcesoFirma>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun registrarFirma(
        procesoId: String,
        firmanteId: String,
        firmaData: String,
        selloTiempo: String,
        ipFirmante: String,
        userAgent: String? = null
    ): FirmaRegistro {
        val procesoActual = verificarEstadoFirma(procesoId)
            ?: throw IllegalArgumentException("Proceso $procesoId no existe")

        require(!procesoActual.yaFirmo(firmanteId)) { "Usuario ya firmó este documento" }
        require(procesoActual.firmantesRequeridos.contains(firmanteId)) { "Usuario no está autorizado a firmar" }

        val firmaRegistro = FirmaRegistro(
            id = generarId(),
            documentoId = procesoActual.documentoId,
            procesoFirmaId = procesoId,
            firmanteId = firmanteId,
            firmaData = firmaData,
            selloTiempo = selloTiempo,
            ipFirmante = ipFirmante,
            userAgent = userAgent,
            certificadoId = null,
            estado = "FIRMADO"
        )

        postgrestClient
            .from(TABLA_FIRMAS_REGISTROS)
            .insert(firmaRegistro)

        val procesoActualizado = procesoActual.copy(
            firmasCompletadas = procesoActual.firmasCompletadas + firmaRegistro,
            estado = if (procesoActual.estaCompleto) EstadoProcesoFirma.COMPLETO else EstadoProcesoFirma.EN_PROGRESO,
            updatedAt = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME),
            completadoEn = if (procesoActual.estaCompleto) LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME) else null
        )

        postgrestClient
            .from(TABLA_PROCESOS_FIRMA)
            .update({
                set("firmas_completadas", procesoActualizado.firmasCompletadas)
                set("estado", procesoActualizado.estado.name.lowercase())
                set("updated_at", procesoActualizado.updatedAt)
                if (procesoActualizado.completadoEn != null) {
                    set("completed_at", procesoActualizado.completadoEn)
                }
            }) {
                filter { eq("id", procesoId) }
            }

        return firmaRegistro
    }

    suspend fun rechazarFirma(
        procesoId: String,
        firmanteId: String,
        motivo: String = "Rechazado por el firmante",
        ipOrigen: String
    ) {
        val procesoActual = verificarEstadoFirma(procesoId)
            ?: throw IllegalArgumentException("Proceso $procesoId no existe")

        require(procesoActual.firmantesRequeridos.contains(firmanteId)) { "Usuario no está autorizado" }

        val firmaRegistro = FirmaRegistro(
            id = generarId(),
            documentoId = procesoActual.documentoId,
            procesoFirmaId = procesoId,
            firmanteId = firmanteId,
            firmaData = "",
            selloTiempo = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME),
            ipFirmante = ipOrigen,
            userAgent = null,
            certificadoId = null,
            estado = "RECHAZADO",
            motivoRechazo = motivo
        )

        postgrestClient
            .from(TABLA_FIRMAS_REGISTROS)
            .insert(firmaRegistro)

        postgrestClient
            .from(TABLA_PROCESOS_FIRMA)
            .update({
                set("estado", EstadoProcesoFirma.CANCELADO.name.lowercase())
                set("updated_at", LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME))
            }) {
                filter { eq("id", procesoId) }
            }
    }

    suspend fun descargarDocumentoFirmado(procesoId: String): ByteArray? = null

    suspend fun validarIntegridad(documentoId: String): ValidationResult {
        return ValidationResult(
            esValido = true,
            mensajes = listOf("Funcionalidad de firma en desarrollo")
        )
    }

    suspend fun obtenerHistorialFirma(documentoId: String): List<FirmaRegistro> {
        return try {
            postgrestClient
                .from(TABLA_FIRMAS_REGISTROS)
                .select { filter { eq("documento_id", documentoId) } }
                .decodeList<FirmaRegistro>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun cancelarProcesoFirma(procesoId: String, razon: String = "Cancelado por el usuario") {
        postgrestClient
            .from(TABLA_PROCESOS_FIRMA)
            .update({
                set("estado", EstadoProcesoFirma.CANCELADO.name.lowercase())
                set("updated_at", LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME))
            }) {
                filter { eq("id", procesoId) }
            }
    }

    data class ValidationResult(
        val esValido: Boolean,
        val mensajes: List<String>,
        val detalles: Map<String, Any> = emptyMap()
    )
}
