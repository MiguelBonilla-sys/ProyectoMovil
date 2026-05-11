package com.example.proyecto.data.repository

import com.example.proyecto.data.model.AuditLog
import com.example.proyecto.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Repositorio para gestionar logs de auditoría.
 * Fase 2: Auditoría y Seguridad
 */
class AuditLogRepository {
    private val supabase = SupabaseClientProvider.client
    private val postgrestClient = supabase.postgrest

    companion object {
        const val TABLA_AUDIT_LOGS = "audit_logs"
        const val PAGE_SIZE = 50
    }

    /**
     * Obtiene todos los logs de auditoría con paginación.
     * Solo disponible para administradores.
     */
    suspend fun obtenerTodos(
        limit: Int = PAGE_SIZE,
        offset: Int = 0,
        ordenar: String = "created_at"
    ): List<AuditLog> {
        return try {
            postgrestClient
                .from(TABLA_AUDIT_LOGS)
                .select {
                    order(ordenar, Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<AuditLog>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene logs de auditoría de un usuario específico.
     */
    suspend fun obtenerPorUsuario(
        usuarioId: String,
        limit: Int = PAGE_SIZE,
        offset: Int = 0
    ): List<AuditLog> {
        return try {
            postgrestClient
                .from(TABLA_AUDIT_LOGS)
                .select {
                    filter { eq("usuario_id", usuarioId) }
                    order("created_at", Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<AuditLog>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene logs de auditoría de una tabla específica.
     */
    suspend fun obtenerPorTabla(
        tabla: String,
        limit: Int = PAGE_SIZE,
        offset: Int = 0
    ): List<AuditLog> {
        return try {
            postgrestClient
                .from(TABLA_AUDIT_LOGS)
                .select {
                    filter { eq("tabla_afectada", tabla) }
                    order("created_at", Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<AuditLog>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene logs de auditoría de una acción específica (INSERT, UPDATE, DELETE).
     */
    suspend fun obtenerPorAccion(
        accion: String,
        limit: Int = PAGE_SIZE,
        offset: Int = 0
    ): List<AuditLog> {
        return try {
            postgrestClient
                .from(TABLA_AUDIT_LOGS)
                .select {
                    filter { eq("accion", accion) }
                    order("created_at", Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<AuditLog>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene logs de auditoría de un registro específico.
     * Útil para ver el historial completo de un documento, consulta, etc.
     */
    suspend fun obtenerHistorialRegistro(
        registroId: String,
        tablaAfectada: String? = null
    ): List<AuditLog> {
        return try {
            postgrestClient
                .from(TABLA_AUDIT_LOGS)
                .select {
                    filter { eq("registro_id", registroId) }
                    if (tablaAfectada != null) {
                        filter { eq("tabla_afectada", tablaAfectada) }
                    }
                    order("created_at", Order.ASCENDING) // Cronológico para ver evolución
                }
                .decodeList<AuditLog>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene logs de auditoría en un rango de fechas.
     */
    suspend fun obtenerPorFechas(
        fechaInicio: String, // ISO 8601 (YYYY-MM-DD)
        fechaFin: String,    // ISO 8601 (YYYY-MM-DD)
        limit: Int = PAGE_SIZE,
        offset: Int = 0
    ): List<AuditLog> {
        return try {
            postgrestClient
                .from(TABLA_AUDIT_LOGS)
                .select {
                    filter { gte("created_at", "${fechaInicio}T00:00:00Z") }
                    filter { lte("created_at", "${fechaFin}T23:59:59Z") }
                    order("created_at", Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<AuditLog>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Busca logs de auditoría por múltiples criterios.
     */
    suspend fun buscar(
        tabla: String? = null,
        accion: String? = null,
        usuarioId: String? = null,
        fechaInicio: String? = null,
        fechaFin: String? = null,
        limit: Int = PAGE_SIZE,
        offset: Int = 0
    ): List<AuditLog> {
        return try {
            postgrestClient
                .from(TABLA_AUDIT_LOGS)
                .select {
                    if (tabla != null) filter { eq("tabla_afectada", tabla) }
                    if (accion != null) filter { eq("accion", accion) }
                    if (usuarioId != null) filter { eq("usuario_id", usuarioId) }
                    if (fechaInicio != null) filter { gte("created_at", "${fechaInicio}T00:00:00Z") }
                    if (fechaFin != null) filter { lte("created_at", "${fechaFin}T23:59:59Z") }
                    order("created_at", Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<AuditLog>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene resumen de actividades por tabla.
     */
    suspend fun obtenerResumen(): Map<String, Map<String, Int>> {
        return try {
            val logs = postgrestClient
                .from(TABLA_AUDIT_LOGS)
                .select()
                .decodeList<AuditLog>()

            // Agrupar por tabla y acción
            logs.groupBy { it.tablaAfectada }
                .mapValues { (_, logsTabla) ->
                    logsTabla.groupingBy { it.accion }
                        .eachCount()
                }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Exporta logs de auditoría como CSV.
     * Útil para análisis externo o compliance.
     */
    suspend fun exportarCSV(
        tabla: String? = null,
        fechaInicio: String? = null,
        fechaFin: String? = null
    ): String {
        val logs = buscar(
            tabla = tabla,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            limit = 10000 // Máximo para export
        )

        val headers = listOf(
            "ID", "Usuario", "Tabla", "Acción", "Registro ID",
            "Datos Anteriores", "Datos Nuevos", "IP", "User Agent", "Fecha"
        )

        val rows = logs.map { log ->
            listOf(
                log.id ?: "",
                log.usuarioId ?: "SISTEMA",
                log.tablaAfectada,
                log.accion,
                log.registroId,
                log.datosAnteriores?.toString() ?: "",
                log.datosNuevos?.toString() ?: "",
                log.ipOrigen ?: "",
                log.userAgent ?: "",
                log.createdAt ?: ""
            )
        }

        // Construir CSV
        val csvContent = StringBuilder()
        csvContent.append(headers.joinToString(",") { escaparCSV(it) }).append("\n")
        rows.forEach { row ->
            csvContent.append(row.joinToString(",") { escaparCSV(it.toString()) }).append("\n")
        }

        return csvContent.toString()
    }

    /**
     * Obtiene actividades recientes (últimas 24 horas).
     */
    suspend fun obtenerActividadesRecientes(limite: Int = 20): List<AuditLog> {
        val hace24h = LocalDateTime.now(ZoneOffset.UTC)
            .minusHours(24)
            .format(DateTimeFormatter.ISO_DATE_TIME)

        return try {
            postgrestClient
                .from(TABLA_AUDIT_LOGS)
                .select {
                    filter { gte("created_at", hace24h) }
                    order("created_at", Order.DESCENDING)
                    range(0, (limite - 1).toLong())
                }
                .decodeList<AuditLog>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene cambios en un documento específico (para ver historial de firma).
     */
    suspend fun obtenerHistorialDocumento(documentoId: String): List<AuditLog> {
        return obtenerHistorialRegistro(documentoId, "documentos")
    }

    /**
     * Obtiene cambios en una consulta específica.
     */
    suspend fun obtenerHistorialConsulta(consultaId: String): List<AuditLog> {
        return obtenerHistorialRegistro(consultaId, "consultas")
    }

    private fun escaparCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
