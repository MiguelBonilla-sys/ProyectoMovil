package com.example.proyecto.data.repository

import com.example.proyecto.data.model.User
import com.example.proyecto.data.storage.FileStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

object UserRepository {
    private val _users: MutableList<User> = mutableListOf()

    /** Inicializa el repositorio. Si ya hay datos guardados los usa; si no, carga la semilla. */
    fun init(seedJson: String) {
        val stored = runCatching { FileStorage().readUsers() }.getOrNull()
        val source = if (!stored.isNullOrBlank()) stored else seedJson
        val parsed = runCatching {
            json.decodeFromString<List<User>>(source)
        }.getOrElse {
            runCatching { json.decodeFromString<List<User>>(seedJson) }.getOrDefault(emptyList())
        }
        _users.clear()
        _users.addAll(parsed)
        // Persistir la semilla la primera vez
        if (stored.isNullOrBlank()) save()
    }

    fun authenticate(email: String, password: String, tipo: String): User? =
        _users.find {
            it.email.equals(email, ignoreCase = true) &&
            it.password == password &&
            it.tipo == tipo
        }

    fun authenticateAbogado(email: String, tarjeta: String, password: String): User? =
        _users.find {
            it.email.equals(email, ignoreCase = true) &&
            it.tarjeta.equals(tarjeta, ignoreCase = true) &&
            it.password == password &&
            it.tipo == "abogado"
        }

    fun register(nombre: String, email: String, password: String): Result<User> {
        if (_users.any { it.email.equals(email, ignoreCase = true) }) {
            return Result.failure(Exception("Email ya registrado"))
        }
        val user = User(nombre = nombre, email = email, password = password, tipo = "cliente")
        _users.add(user)
        save()
        return Result.success(user)
    }

    private fun save() {
        runCatching { FileStorage().writeUsers(json.encodeToString(_users)) }
    }
}
