package com.example.proyecto.data.repository

import com.example.proyecto.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReporteDocumentosFirmados(
    @SerialName("mes")            val mes: String,
    @SerialName("total_firmados") val totalFirmados: Long
)

@Serializable
data class ReporteConsultasAbogado(
    @SerialName("abogado_id")          val abogadoId: String,
    @SerialName("abogado_nombre")      val abogadoNombre: String,
    @SerialName("especialidad")        val especialidad: String?,
    @SerialName("total_consultas")     val totalConsultas: Long,
    @SerialName("consultas_cerradas")  val consultasCerradas: Long
)

@Serializable
data class ReporteResumenGeneral(
    @SerialName("total_clientes")   val totalClientes: Long,
    @SerialName("total_abogados")   val totalAbogados: Long,
    @SerialName("total_consultas")  val totalConsultas: Long,
    @SerialName("consultas_abiertas") val consultasAbiertas: Long,
    @SerialName("total_documentos") val totalDocumentos: Long,
    @SerialName("total_plantillas") val totalPlantillas: Long
)

class ReporteRepository {

    suspend fun obtenerDocumentosFirmados(): List<ReporteDocumentosFirmados> =
        supabase.from("reporte_documentos_firmados")
            .select()
            .decodeList<ReporteDocumentosFirmados>()

    suspend fun obtenerConsultasPorAbogado(): List<ReporteConsultasAbogado> =
        supabase.from("reporte_consultas_abogado")
            .select()
            .decodeList<ReporteConsultasAbogado>()

    suspend fun obtenerResumenGeneral(): ReporteResumenGeneral? =
        supabase.from("reporte_resumen_general")
            .select()
            .decodeSingleOrNull<ReporteResumenGeneral>()
}
