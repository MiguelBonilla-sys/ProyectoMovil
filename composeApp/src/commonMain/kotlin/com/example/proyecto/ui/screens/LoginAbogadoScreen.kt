package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.navigation.NavController
import com.example.proyecto.data.AppContainer
import com.example.proyecto.data.session.SessionManager
import com.example.proyecto.navigation.Routes
import com.example.proyecto.ui.viewmodel.AuthViewModel

@Composable
fun LoginAbogadoScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { AuthViewModel(AppContainer.userRepository, coroutineScope) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var tarjetaProfesional by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF3949AB),
        unfocusedBorderColor = Color(0xFF424242),
        focusedLabelColor = Color(0xFF3949AB),
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        cursorColor = Color(0xFF3949AB),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLeadingIconColor = Color(0xFF3949AB),
        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
        focusedTrailingIconColor = Color(0xFF3949AB),
        unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .safeContentPadding()
        ) {
            // ── Header con back ──
            Spacer(modifier = Modifier.height(8.dp))
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Accede a tu cuenta profesional",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Badge diferenciador de rol ──
            Spacer(modifier = Modifier.height(12.dp))
            AssistChip(
                onClick = {},
                label = { Text("Cuenta Profesional") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF00BFA5)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFF1E1E2E),
                    labelColor = Color(0xFF00BFA5)
                )
            )

            // ── Campos del formulario ──
            Spacer(modifier = Modifier.height(24.dp))

            // Campo email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = {
                    Icon(Icons.Filled.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo tarjeta profesional
            OutlinedTextField(
                value = tarjetaProfesional,
                onValueChange = { tarjetaProfesional = it },
                label = { Text("No. Tarjeta Profesional") },
                leadingIcon = {
                    Icon(Icons.Filled.Badge, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo contraseña con toggle visibilidad
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar contraseña"
                            else "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors
            )

            // "¿Olvidaste tu contraseña?"
            TextButton(
                onClick = { /* TODO: recuperar contraseña */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    "¿Olvidaste tu contraseña?",
                    color = Color(0xFF3949AB),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // ── Botón principal ──
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.loginAbogado(
                        email = email.trim(),
                        tarjeta = tarjetaProfesional.trim(),
                        password = password,
                        onSuccess = { user ->
                            SessionManager.login(user)
                            navController.navigate(Routes.MAIN) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            errorMsg = error
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A237E)
                ),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Acceder como Abogado",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            // ── Error message ──
            if (errorMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMsg,
                    color = Color(0xFFCF6679),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Link a registro
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿No tienes cuenta? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { /* TODO: registro abogado */ }) {
                    Text(
                        "Regístrate",
                        color = Color(0xFF3949AB),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer adicional
            Text(
                text = "Solo abogados registrados en el sistema\npueden acceder",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
