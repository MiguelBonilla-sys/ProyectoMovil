package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.data.repository.AuthRepository
import com.example.proyecto.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SecurityUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class SecurityViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    fun cambiarPassword(actual: String, nueva: String, confirmar: String) {
        if (actual.isBlank() || nueva.isBlank() || confirmar.isBlank()) {
            _uiState.value = SecurityUiState(error = "Completa todos los campos")
            return
        }
        if (nueva.length < 6) {
            _uiState.value = SecurityUiState(error = "La nueva contraseña debe tener al menos 6 caracteres")
            return
        }
        if (nueva != confirmar) {
            _uiState.value = SecurityUiState(error = "Las contraseñas no coinciden")
            return
        }
        val userId = SessionManager.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.value = SecurityUiState(isLoading = true)
            try {
                repository.actualizarPassword(userId, actual, nueva)
                _uiState.value = SecurityUiState(success = true)
            } catch (e: Exception) {
                _uiState.value = SecurityUiState(error = e.message ?: "Error al cambiar contraseña")
            }
        }
    }

    fun clearState() { _uiState.value = SecurityUiState() }
}

@Composable
fun SecurityScreen(
    navController: NavController,
    viewModel: SecurityViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var showActual by remember { mutableStateOf(false) }
    var showNueva by remember { mutableStateOf(false) }
    var showConfirmar by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            kotlinx.coroutines.delay(1500)
            navController.popBackStack()
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF3949AB),
        unfocusedBorderColor = Color(0xFF424242),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color(0xFF3949AB),
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        cursorColor = Color(0xFF3949AB),
        focusedLeadingIconColor = Color(0xFF3949AB),
        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.6f),
        focusedTrailingIconColor = Color(0xFF3949AB),
        unfocusedTrailingIconColor = Color.White.copy(alpha = 0.6f)
    )

    Scaffold(
        containerColor = Color(0xFF0D0D1A),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Seguridad", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E2E))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text("Cambiar contraseña", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = Color.White)

            OutlinedTextField(
                value = actual, onValueChange = { actual = it },
                label = { Text("Contraseña actual") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showActual = !showActual }) {
                        Icon(if (showActual) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                    }
                },
                visualTransformation = if (showActual) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = fieldColors
            )

            OutlinedTextField(
                value = nueva, onValueChange = { nueva = it },
                label = { Text("Nueva contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showNueva = !showNueva }) {
                        Icon(if (showNueva) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                    }
                },
                visualTransformation = if (showNueva) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = fieldColors
            )

            OutlinedTextField(
                value = confirmar, onValueChange = { confirmar = it },
                label = { Text("Confirmar nueva contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showConfirmar = !showConfirmar }) {
                        Icon(if (showConfirmar) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                    }
                },
                visualTransformation = if (showConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = fieldColors
            )

            if (uiState.error != null) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFCF6679).copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()) {
                    Text(uiState.error!!, color = Color(0xFFCF6679), modifier = Modifier.padding(12.dp))
                }
            }

            if (uiState.success) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF00BFA5).copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Contraseña actualizada correctamente", color = Color(0xFF00BFA5),
                        modifier = Modifier.padding(12.dp))
                }
            }

            Button(
                onClick = { viewModel.cambiarPassword(actual, nueva, confirmar) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Actualizar contraseña", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
