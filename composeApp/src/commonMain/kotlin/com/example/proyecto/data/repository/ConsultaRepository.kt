package com.example.proyecto.data.repository

import com.example.proyecto.data.model.Consulta
import com.example.proyecto.data.model.ConsultaAbogado
import com.example.proyecto.data.model.EstadoConsulta
import com.example.proyecto.data.remote.supabase
import io.github.jan.supabase.postgrest.from

class ConsultaRepository {

    companion object {
        const val PAGE_SIZE = 20
    }

    suspend fun crearConsulta(consulta: Consulta): Consulta =
        supabase.from("consultas")
            .insert(consulta) { select() }
            .decodeSingle<Consulta>()

    suspend fun obtenerPorCliente(clienteId: String, limit: Int = PAGE_SIZE, offset: Int = 0): List<Consulta> =
        supabase.from("consultas")
            .select {
                filter { eq("cliente_id", clienteId) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<Consulta>()

    suspend fun obtenerPorAbogado(abogadoId: String, limit: Int = PAGE_SIZE, offset: Int = 0): List<Consulta> =
        supabase.from("consultas")
            .select {
                filter { eq("consulta_abogados.abogado_id", abogadoId) }
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<Consulta>()

    suspend fun obtenerPorId(id: String): Consulta? =
        supabase.from("consultas")
            .select {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<Consulta>()

    suspend fun obtenerTodas(limit: Int = PAGE_SIZE, offset: Int = 0): List<Consulta> =
        supabase.from("consultas")
            .select {
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<Consulta>()

    suspend fun actualizarEstado(consultaId: String, nuevoEstado: EstadoConsulta): Consulta =
        supabase.from("consultas")
            .update({ set("estado", nuevoEstado.name.lowercase()) }) {
                filter { eq("id", consultaId) }
                select()
            }
            .decodeSingle<Consulta>()

    suspend fun asignarAbogado(consultaId: String, abogadoId: String): ConsultaAbogado =
        supabase.from("consulta_abogados")
            .insert(ConsultaAbogado(consultaId = consultaId, abogadoId = abogadoId)) { select() }
            .decodeSingle<ConsultaAbogado>()

    suspend fun obtenerAbogadosDeConsulta(consultaId: String): List<ConsultaAbogado> =
        supabase.from("consulta_abogados")
            .select {
                filter { eq("consulta_id", consultaId) }
            }
            .decodeList<ConsultaAbogado>()

    suspend fun filtrarPorEstado(estado: EstadoConsulta): List<Consulta> =
        supabase.from("consultas")
            .select {
                filter { eq("estado", estado.name.lowercase()) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<Consulta>()

    suspend fun filtrarPorArea(area: String): List<Consulta> =
        supabase.from("consultas")
            .select {
                filter { eq("area_practica", area) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<Consulta>()

    suspend fun eliminarConsulta(consultaId: String) {
        supabase.from("consulta_abogados").delete { filter { eq("consulta_id", consultaId) } }
        supabase.from("consultas").delete { filter { eq("id", consultaId) } }
    }
}
