package com.example.proyecto.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TipoPlantilla(val displayName: String) {
    @SerialName("poder_notarial")   PODER_NOTARIAL("Poder Notarial General"),
    @SerialName("contrato_arriendo") CONTRATO_ARRENDAMIENTO("Contrato de Arrendamiento"),
    @SerialName("carta_autorizacion") CARTA_AUTORIZACION("Carta de Autorización"),
    @SerialName("nda")              NDA("Acuerdo de Confidencialidad (NDA)"),
    @SerialName("demanda_civil")    DEMANDA_CIVIL("Demanda Civil")
}

@Serializable
data class CampoPlantilla(
    val clave: String,
    val etiqueta: String,
    val tipo: String,
    val requerido: Boolean = false,
    val opciones: List<String> = emptyList(),
    val placeholder: String = ""
)

@Serializable
data class DatosPlantilla(
    val tipo: String,
    val campos: Map<String, String> = emptyMap()
)