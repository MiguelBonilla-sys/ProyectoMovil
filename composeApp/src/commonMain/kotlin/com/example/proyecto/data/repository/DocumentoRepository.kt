package com.example.proyecto.data.repository

import com.example.proyecto.data.model.Documento
import com.example.proyecto.data.model.EstadoFirma
import com.example.proyecto.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage

class DocumentoRepository {

    companion object {
        const val BUCKET_DOCUMENTOS = "documentos"
        const val BUCKET_PLANTILLAS = "plantillas"
        const val PAGE_SIZE = 20
    }

    suspend fun subirDocumento(
        nombre: String,
        bytes: ByteArray,
        consultaId: String?,
        tipo: String,
        subidoPor: String,
        esPlantilla: Boolean = false
    ): Documento {
        val bucket = if (esPlantilla) BUCKET_PLANTILLAS else BUCKET_DOCUMENTOS
        val path = "$subidoPor/${System.currentTimeMillis()}_$nombre"

        supabase.storage.from(bucket).upload(path, bytes) {
            upsert = false
        }

        val publicUrl = supabase.storage.from(bucket).publicUrl(path)

        val documento = Documento(
            consultaId = consultaId,
            nombre = nombre,
            url = publicUrl,
            tipo = tipo,
            esPlantilla = esPlantilla,
            subidoPor = subidoPor
        )

        return supabase.from("documentos")
            .insert(documento) { select() }
            .decodeSingle<Documento>()
    }

    suspend fun crearDesdeMetadata(
        nombre: String,
        tipo: String,
        subidoPor: String,
        consultaId: String? = null,
        url: String = ""
    ): Documento {
        val documento = Documento(
            consultaId = consultaId,
            nombre = nombre,
            url = url,
            tipo = tipo,
            esPlantilla = false,
            subidoPor = subidoPor
        )
        return supabase.from("documentos")
            .insert(documento) { select() }
            .decodeSingle<Documento>()
    }

    suspend fun obtenerPlantillas(limit: Int = PAGE_SIZE, offset: Int = 0): List<Documento> =
        supabase.from("documentos")
            .select {
                filter { eq("es_plantilla", true) }
                order("created_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<Documento>()

    suspend fun obtenerPorConsulta(consultaId: String): List<Documento> =
        supabase.from("documentos")
            .select {
                filter { eq("consulta_id", consultaId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Documento>()

    suspend fun obtenerPorUsuario(userId: String, limit: Int = PAGE_SIZE, offset: Int = 0): List<Documento> =
        supabase.from("documentos")
            .select {
                filter { eq("subido_por", userId) }
                order("created_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<Documento>()

    suspend fun actualizarEstadoFirma(documentoId: String, nuevoEstado: EstadoFirma): Documento =
        supabase.from("documentos")
            .update({ set("estado_firma", nuevoEstado.name.lowercase()) }) {
                filter { eq("id", documentoId) }
                select()
            }
            .decodeSingle<Documento>()

    suspend fun descargarDocumento(url: String): ByteArray {
        val path = extraerPathDeUrl(url)
        val bucket = if (url.contains(BUCKET_PLANTILLAS)) BUCKET_PLANTILLAS else BUCKET_DOCUMENTOS
        return supabase.storage.from(bucket).downloadPublic(path)
    }

    suspend fun eliminarDocumento(documentoId: String, url: String) {
        val path = extraerPathDeUrl(url)
        val bucket = if (url.contains(BUCKET_PLANTILLAS)) BUCKET_PLANTILLAS else BUCKET_DOCUMENTOS
        supabase.storage.from(bucket).delete(listOf(path))
        supabase.from("documentos").delete {
            filter { eq("id", documentoId) }
        }
    }

    private fun extraerPathDeUrl(url: String): String {
        val marker = "/object/public/${BUCKET_DOCUMENTOS}/"
        val markerPlantillas = "/object/public/${BUCKET_PLANTILLAS}/"
        return when {
            url.contains(marker) -> url.substringAfter(marker)
            url.contains(markerPlantillas) -> url.substringAfter(markerPlantillas)
            else -> url.substringAfterLast("/")
        }
    }
}
