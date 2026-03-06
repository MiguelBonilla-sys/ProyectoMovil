package com.example.proyecto.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val nombre: String,
    val email: String,
    val password: String,
    val tipo: String,       // "cliente" | "abogado"
    val tarjeta: String = ""
) {
    val initials: String
        get() = nombre
            .split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
}
