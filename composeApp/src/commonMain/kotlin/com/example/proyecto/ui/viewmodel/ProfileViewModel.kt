package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.User
import com.example.proyecto.data.remote.supabase
import com.example.proyecto.data.repository.AuthRepository
import com.example.proyecto.data.session.SessionManager
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val usuario: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = ProfileUiState(usuario = SessionManager.currentUser)
    }

    fun actualizarPerfil(
        nombre: String,
        telefono: String?,
        descripcion: String?,
        especialidad: String?,
        experiencia: Int?
    ) {
        val userId = SessionManager.currentUser?.id ?: return
        if (nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "El nombre no puede estar vacío")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, successMessage = null)
            try {
                supabase.from("users")
                    .update({
                        set("nombre", nombre)
                        telefono?.let { set("telefono", it) }
                        descripcion?.let { set("descripcion", it) }
                        especialidad?.let { set("especialidad", it) }
                        experiencia?.let { set("experiencia", it) }
                    }) {
                        filter { eq("id", userId) }
                    }

                val current = SessionManager.currentUser!!
                val actualizado = current.copy(
                    nombre = nombre,
                    telefono = telefono ?: current.telefono,
                    descripcion = descripcion ?: current.descripcion,
                    especialidad = especialidad ?: current.especialidad,
                    experiencia = experiencia ?: current.experiencia
                )
                SessionManager.login(actualizado)
                _uiState.value = ProfileUiState(
                    usuario = actualizado,
                    isSaving = false,
                    successMessage = "Perfil actualizado correctamente"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Error al actualizar perfil"
                )
            }
        }
    }

    fun eliminarCuenta(onSuccess: () -> Unit) {
        val userId = SessionManager.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                authRepository.eliminarUsuario(userId)
                SessionManager.logout()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Error al eliminar la cuenta"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
