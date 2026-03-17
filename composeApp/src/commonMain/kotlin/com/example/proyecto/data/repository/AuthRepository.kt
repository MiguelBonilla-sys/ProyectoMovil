package com.example.proyecto.data.repository

import com.example.proyecto.data.model.User
import com.example.proyecto.data.remote.supabase
import com.example.proyecto.data.util.SecurityUtils
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator

class AuthRepository {

    suspend fun registrarCliente(
        nombre: String,
        email: String,
        password: String
    ): User {
        val existing = obtenerPorEmail(email)
        if (existing != null) throw Exception("El correo ya está registrado")

        val hashedPassword = SecurityUtils.hashPassword(password)
        val usuario = User(
            nombre = nombre,
            email = email,
            password = hashedPassword,
            tipo = "cliente"
        )
        return supabase.from("users")
            .insert(usuario) { select() }
            .decodeSingle<User>()
    }

    suspend fun registrarAbogado(
        nombre: String,
        email: String,
        password: String,
        tarjeta: String,
        especialidad: String,
        experiencia: Int,
        descripcion: String,
        telefono: String
    ): User {
        val existing = obtenerPorEmail(email)
        if (existing != null) throw Exception("El correo ya está registrado")

        val hashedPassword = SecurityUtils.hashPassword(password)
        val usuario = User(
            nombre = nombre,
            email = email,
            password = hashedPassword,
            tipo = "abogado",
            tarjeta = tarjeta,
            especialidad = especialidad,
            experiencia = experiencia,
            descripcion = descripcion,
            telefono = telefono
        )
        return supabase.from("users")
            .insert(usuario) { select() }
            .decodeSingle<User>()
    }

    suspend fun autenticarCliente(email: String, password: String): User? {
        val usuario = obtenerPorEmail(email) ?: return null
        if (usuario.tipo != "cliente") return null
        return if (SecurityUtils.verifyPassword(password, usuario.password)) usuario else null
    }

    suspend fun autenticarAbogado(email: String, tarjeta: String, password: String): User? {
        val usuario = obtenerPorEmail(email) ?: return null
        if (usuario.tipo != "abogado") return null
        if (usuario.tarjeta != tarjeta) return null
        return if (SecurityUtils.verifyPassword(password, usuario.password)) usuario else null
    }

    private suspend fun obtenerPorEmail(email: String): User? =
        supabase.from("users")
            .select {
                filter {
                    eq("email", email)
                }
            }
            .decodeSingleOrNull<User>()
}
