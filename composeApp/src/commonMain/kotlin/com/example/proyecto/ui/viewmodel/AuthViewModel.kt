package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.User
import com.example.proyecto.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loginCliente(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            val msg = "Por favor completa todos los campos"
            _error.value = msg; onError(msg); return
        }
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val user = repository.autenticarCliente(email.trim(), password)
                _isLoading.value = false
                if (user != null) onSuccess(user)
                else { val msg = "Credenciales incorrectas"; _error.value = msg; onError(msg) }
            } catch (e: Exception) {
                _isLoading.value = false
                val msg = e.message ?: "Error al iniciar sesión"
                _error.value = msg; onError(msg)
            }
        }
    }

    fun loginAbogado(
        email: String,
        tarjeta: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || tarjeta.isBlank() || password.isBlank()) {
            val msg = "Por favor completa todos los campos"
            _error.value = msg; onError(msg); return
        }
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val user = repository.autenticarAbogado(email.trim(), tarjeta.trim(), password)
                _isLoading.value = false
                if (user != null) onSuccess(user)
                else { val msg = "Credenciales incorrectas"; _error.value = msg; onError(msg) }
            } catch (e: Exception) {
                _isLoading.value = false
                val msg = e.message ?: "Error al iniciar sesión"
                _error.value = msg; onError(msg)
            }
        }
    }

    fun registerCliente(
        nombre: String,
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            val msg = "Por favor completa todos los campos"
            _error.value = msg; onError(msg); return
        }
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val user = repository.registrarCliente(nombre.trim(), email.trim(), password)
                _isLoading.value = false
                onSuccess(user)
            } catch (e: Exception) {
                _isLoading.value = false
                val msg = e.message ?: "Error al registrar"
                _error.value = msg; onError(msg)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
