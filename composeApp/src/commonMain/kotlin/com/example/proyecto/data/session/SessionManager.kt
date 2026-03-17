package com.example.proyecto.data.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.proyecto.data.model.User
import com.example.proyecto.data.storage.LocalStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val KEY_USER = "session_user"

object SessionManager {

    var currentUser: User? by mutableStateOf(null)
        private set

    fun login(user: User) {
        currentUser = user
        LocalStorage.putString(KEY_USER, Json.encodeToString(user))
    }

    fun logout() {
        currentUser = null
        LocalStorage.remove(KEY_USER)
    }

    /** Llama esto al arrancar la app para restaurar sesión guardada.
     *  Retorna true si había sesión guardada. */
    fun restoreSession(): Boolean {
        val json = LocalStorage.getString(KEY_USER) ?: return false
        return try {
            currentUser = Json.decodeFromString<User>(json)
            true
        } catch (_: Exception) {
            LocalStorage.remove(KEY_USER)
            false
        }
    }
}
