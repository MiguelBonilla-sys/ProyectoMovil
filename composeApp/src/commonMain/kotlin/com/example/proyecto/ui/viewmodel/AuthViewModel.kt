package com.example.proyecto.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.proyecto.data.model.User
import com.example.proyecto.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication operations.
 * 
 * Manages login and registration state for both clients and lawyers.
 * Uses UserRepository for data operations with offline-first architecture.
 * 
 * @param repository UserRepository instance for data access
 * @param coroutineScope Scope for launching coroutines
 */
class AuthViewModel(
    private val repository: UserRepository,
    private val coroutineScope: CoroutineScope
) {
    
    // ========== STATE ==========
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    // ========== AUTHENTICATION ==========
    
    /**
     * Authenticate a client with email and password.
     * 
     * @param email Client's email
     * @param password Client's password
     * @param onSuccess Callback with authenticated user
     * @param onError Callback with error message
     */
    fun loginCliente(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            error = "Por favor completa todos los campos"
            onError("Por favor completa todos los campos")
            return
        }
        
        isLoading = true
        error = null
        
        coroutineScope.launch {
            try {
                val user = repository.authenticateCliente(email, password)
                
                isLoading = false
                
                if (user != null) {
                    onSuccess(user)
                } else {
                    error = "Credenciales incorrectas"
                    onError("Credenciales incorrectas")
                }
            } catch (e: Exception) {
                isLoading = false
                error = e.message ?: "Error al iniciar sesión"
                onError(error!!)
            }
        }
    }
    
    /**
     * Authenticate a lawyer with email, tarjeta profesional, and password.
     * 
     * @param email Lawyer's email
     * @param tarjeta Lawyer's professional card number
     * @param password Lawyer's password
     * @param onSuccess Callback with authenticated user
     * @param onError Callback with error message
     */
    fun loginAbogado(
        email: String,
        tarjeta: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || tarjeta.isBlank() || password.isBlank()) {
            error = "Por favor completa todos los campos"
            onError("Por favor completa todos los campos")
            return
        }
        
        isLoading = true
        error = null
        
        coroutineScope.launch {
            try {
                val user = repository.authenticateAbogado(email, tarjeta, password)
                
                isLoading = false
                
                if (user != null) {
                    onSuccess(user)
                } else {
                    error = "Credenciales incorrectas"
                    onError("Credenciales incorrectas")
                }
            } catch (e: Exception) {
                isLoading = false
                error = e.message ?: "Error al iniciar sesión"
                onError(error!!)
            }
        }
    }
    
    // ========== REGISTRATION ==========
    
    /**
     * Register a new client.
     * 
     * @param nombre Client's full name
     * @param email Client's email
     * @param password Client's password
     * @param onSuccess Callback with registered user
     * @param onError Callback with error message
     */
    fun registerCliente(
        nombre: String,
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            error = "Por favor completa todos los campos"
            onError("Por favor completa todos los campos")
            return
        }
        
        isLoading = true
        error = null
        
        coroutineScope.launch {
            try {
                val result = repository.registerCliente(nombre, email, password)
                
                isLoading = false
                
                result.fold(
                    onSuccess = { user ->
                        onSuccess(user)
                    },
                    onFailure = { exception ->
                        error = exception.message ?: "Error al registrar"
                        onError(error!!)
                    }
                )
            } catch (e: Exception) {
                isLoading = false
                error = e.message ?: "Error al registrar"
                onError(error!!)
            }
        }
    }
    
    // ========== UTILITY ==========
    
    /**
     * Clear error state.
     */
    fun clearError() {
        error = null
    }
}
