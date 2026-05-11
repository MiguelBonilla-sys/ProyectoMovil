package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.repository.AuthRepository
import com.example.proyecto.data.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado UI para la pantalla de recuperación de contraseña.
 * Fase 1: Recuperación de Contraseña
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val step: ForgotPasswordStep = ForgotPasswordStep.SOLICITUD, // SOLICITUD, VERIFICACION, CONFIRMACION
    val emailEnviado: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val emailValido: Boolean = false,
    
    // Para paso 2: verificación de token
    val token: String = "",
    val tokenValido: Boolean = false,
    
    // Para paso 3: nueva contraseña
    val nuevaContrasena: String = "",
    val confirmarContrasena: String = "",
    val contrasenaValida: Boolean = false,
    val contraseniasCoinciden: Boolean = false
)

enum class ForgotPasswordStep {
    SOLICITUD,      // Ingresar email
    VERIFICACION,   // Ingresar token recibido
    CONFIRMACION    // Ingresar nueva contraseña
}

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el email en el campo de entrada.
     */
    fun actualizarEmail(email: String) {
        val esValido = ValidationUtils.isValidEmail(email)
        _uiState.value = _uiState.value.copy(
            email = email,
            emailValido = esValido,
            error = null
        )
    }

    /**
     * Solicita envío de email de recuperación.
     */
    fun solicitarRecuperacion() {
        val email = _uiState.value.email.trim()

        if (email.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Ingresa tu correo electrónico")
            return
        }

        if (!ValidationUtils.isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(error = "Email inválido")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Llamar al repository para iniciar proceso de recuperación
                authRepository.solicitarRecuperacionContrasena(email)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    step = ForgotPasswordStep.VERIFICACION,
                    emailEnviado = true,
                    successMessage = "Email de recuperación enviado a $email"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al solicitar recuperación"
                )
            }
        }
    }

    /**
     * Actualiza el token ingresado.
     */
    fun actualizarToken(token: String) {
        _uiState.value = _uiState.value.copy(
            token = token,
            tokenValido = token.trim().length >= 10,
            error = null
        )
    }

    /**
     * Verifica el token de recuperación.
     */
    fun verificarToken() {
        val token = _uiState.value.token.trim()

        if (token.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Ingresa el código de recuperación")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Llamar al repository para verificar token
                authRepository.verificarTokenRecuperacion(_uiState.value.email, token)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    step = ForgotPasswordStep.CONFIRMACION,
                    tokenValido = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Token inválido o expirado"
                )
            }
        }
    }

    /**
     * Actualiza la nueva contraseña.
     */
    fun actualizarNuevaContrasena(contrasena: String) {
        val esValida = ValidationUtils.isValidPassword(contrasena)
        val coincide = contrasena == _uiState.value.confirmarContrasena

        _uiState.value = _uiState.value.copy(
            nuevaContrasena = contrasena,
            contrasenaValida = esValida,
            contraseniasCoinciden = coincide,
            error = null
        )
    }

    /**
     * Actualiza la confirmación de contraseña.
     */
    fun actualizarConfirmarContrasena(confirmacion: String) {
        val coincide = confirmacion == _uiState.value.nuevaContrasena

        _uiState.value = _uiState.value.copy(
            confirmarContrasena = confirmacion,
            contraseniasCoinciden = coincide,
            error = null
        )
    }

    /**
     * Confirma la nueva contraseña.
     */
    fun confirmarNuevaContrasena() {
        val nuevaPwd = _uiState.value.nuevaContrasena
        val confirmacion = _uiState.value.confirmarContrasena

        if (nuevaPwd.isEmpty() || confirmacion.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Completa todos los campos")
            return
        }

        if (!ValidationUtils.isValidPassword(nuevaPwd)) {
            _uiState.value = _uiState.value.copy(
                error = "Contraseña debe tener: 8+ caracteres, mayúscula, minúscula, número"
            )
            return
        }

        if (nuevaPwd != confirmacion) {
            _uiState.value = _uiState.value.copy(error = "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Llamar al repository para actualizar contraseña
                authRepository.restablecerContrasena(
                    email = _uiState.value.email,
                    token = _uiState.value.token,
                    nuevaContrasena = nuevaPwd
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Contraseña restablecida exitosamente"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al actualizar contraseña"
                )
            }
        }
    }

    /**
     * Regresa al paso anterior.
     */
    fun irAtras() {
        when (_uiState.value.step) {
            ForgotPasswordStep.VERIFICACION -> {
                _uiState.value = _uiState.value.copy(
                    step = ForgotPasswordStep.SOLICITUD,
                    token = "",
                    error = null
                )
            }
            ForgotPasswordStep.CONFIRMACION -> {
                _uiState.value = _uiState.value.copy(
                    step = ForgotPasswordStep.VERIFICACION,
                    nuevaContrasena = "",
                    confirmarContrasena = "",
                    error = null
                )
            }
            else -> {} // No hacer nada en SOLICITUD
        }
    }

    /**
     * Limpia mensajes.
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}
