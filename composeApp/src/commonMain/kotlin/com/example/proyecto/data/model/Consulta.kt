package com.example.proyecto.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EstadoConsulta(val display: String) {
    @SerialName("abierta")   ABIERTA("Abierta"),
    @SerialName("en_curso")  EN_CURSO("En Curso"),
    @SerialName("cerrada")   CERRADA("Cerrada")
}

@Serializable
data class Consulta(
    @SerialName("id")
    val id: String? = null,

    @SerialName("cliente_id")
    val clienteId: String,

    @SerialName("estado")
    val estado: EstadoConsulta = EstadoConsulta.ABIERTA,

    @SerialName("area_practica")
    val areaPractica: String,

    @SerialName("descripcion")
    val descripcion: String,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class ConsultaAbogado(
    @SerialName("consulta_id")
    val consultaId: String,

    @SerialName("abogado_id")
    val abogadoId: String,

    @SerialName("asignado_en")
    val asignadoEn: String? = null
)
