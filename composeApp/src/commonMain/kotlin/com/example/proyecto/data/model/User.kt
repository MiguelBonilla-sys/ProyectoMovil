package com.example.proyecto.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id")
    val id: String? = null,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String,

    /** "cliente" | "abogado" | "administrador" */
    @SerialName("tipo")
    val tipo: String,

    @SerialName("tarjeta")
    val tarjeta: String = "",

    // ── Campos específicos de cliente (RF1) ──
    @SerialName("preferencias_areas")
    val preferenciasAreas: List<String> = emptyList(),

    @SerialName("tipo_tramites")
    val tipoTramites: List<String> = emptyList(),

    // ── Campos específicos de abogado (RF2) ──
    @SerialName("especialidad")
    val especialidad: String? = null,

    @SerialName("experiencia")
    val experiencia: Int? = null,

    @SerialName("descripcion")
    val descripcion: String? = null,

    @SerialName("telefono")
    val telefono: String? = null,

    @SerialName("calificacion_promedio")
    val calificacionPromedio: Double? = null,

    @SerialName("foto_url")
    val fotoUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    val initials: String
        get() = nombre
            .split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")

    val esCliente get() = tipo == "cliente"
    val esAbogado get() = tipo == "abogado"
    val esAdmin get() = tipo == "administrador"
}
