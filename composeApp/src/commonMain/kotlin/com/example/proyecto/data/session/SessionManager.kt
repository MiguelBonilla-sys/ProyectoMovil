package com.example.proyecto.data.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.proyecto.data.model.User

object SessionManager {
    var currentUser: User? by mutableStateOf(null)
        private set

    fun login(user: User) {
        currentUser = user
    }

    fun logout() {
        currentUser = null
    }
}
