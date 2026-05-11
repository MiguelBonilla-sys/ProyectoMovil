package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.ui.viewmodel.ForgotPasswordStep
import com.example.proyecto.ui.viewmodel.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)

/**
 * Pantalla de recuperación de contraseña.
 * Flujo de 3 pasos:
 * 1. Ingresar email
 * 2. Ingresar token (código enviado al email)
 * 3. Ingresar nueva contraseña
 */
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit = {},
    onRecuperacionExitosa: () -> Unit = {},
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null && uiState.successMessage!!.contains("exitosamente")) {
            // Esperar un segundo y navegar back
            kotlinx.coroutines.delay(1500)
            onRecuperacionExitosa()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E2E)
                )
            )
        },
        containerColor = Color(0xFF0D0D1A)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Indicador de progreso
            item {
                ProgressIndicatorSteps(
                    currentStep = when (uiState.step) {
                        ForgotPasswordStep.SOLICITUD -> 1
                        ForgotPasswordStep.VERIFICACION -> 2
                        ForgotPasswordStep.CONFIRMACION -> 3
                    },
                    totalSteps = 3,
                    labels = listOf("Email", "Código", "Contraseña")
                )
            }

            // Contenido según el paso actual
            item {
                when (uiState.step) {
                    ForgotPasswordStep.SOLICITUD -> {
                        SolicitudEmailStep(
                            uiState = uiState,
                            onEmailChange = { viewModel.actualizarEmail(it) },
                            onSolicitar = { viewModel.solicitarRecuperacion() },
                            onAtras = { onNavigateBack() },
                            viewModel = viewModel
                        )
                    }

                    ForgotPasswordStep.VERIFICACION -> {
                        VerificacionTokenStep(
                            uiState = uiState,
                            onTokenChange = { viewModel.actualizarToken(it) },
                            onVerificar = { viewModel.verificarToken() },
                            onAtras = { viewModel.irAtras() },
                            viewModel = viewModel
                        )
                    }

                    ForgotPasswordStep.CONFIRMACION -> {
                        ConfirmacionContrasenaStep(
                            uiState = uiState,
                            onNuevaContrasenaChange = { viewModel.actualizarNuevaContrasena(it) },
                            onConfirmarContrasenaChange = { viewModel.actualizarConfirmarContrasena(it) },
                            onConfirmar = { viewModel.confirmarNuevaContrasena() },
                            onAtras = { viewModel.irAtras() },
                            viewModel = viewModel
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SolicitudEmailStep(
    uiState: com.example.proyecto.ui.viewmodel.ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSolicitar: () -> Unit,
    onAtras: () -> Unit,
    viewModel: ForgotPasswordViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
            Text(
                "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                "Ingresa tu correo electrónico y te enviaremos instrucciones para recuperar tu cuenta.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            // Mostrar error si existe
            if (uiState.error != null) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color(0xFFCF6679).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = Color(0xFFCF6679),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            uiState.error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCF6679),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico") },
                placeholder = { Text("tu@email.com") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = uiState.email.isNotEmpty() && !uiState.emailValido,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3949AB),
                    unfocusedBorderColor = Color(0xFF3949AB).copy(alpha = 0.5f)
                )
            )

            Button(
                onClick = onSolicitar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = uiState.emailValido && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3949AB)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enviar enlace de recuperación")
                }
            }

            Button(
                onClick = onAtras,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3949AB).copy(alpha = 0.3f)
                )
            ) {
                Text("Cancelar", color = Color.White)
            }
        }
    }

        @Composable
    private fun VerificacionTokenStep(
    uiState: com.example.proyecto.ui.viewmodel.ForgotPasswordUiState,
    onTokenChange: (String) -> Unit,
    onVerificar: () -> Unit,
    onAtras: () -> Unit,
    viewModel: ForgotPasswordViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
            Text(
                "Verifica tu código",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                "Hemos enviado un código a ${uiState.email}. Ingresa el código para continuar.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            if (uiState.error != null) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color(0xFFCF6679).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = Color(0xFFCF6679),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            uiState.error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCF6679),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.token,
                onValueChange = onTokenChange,
                label = { Text("Código de verificación") },
                placeholder = { Text("000000") },
                leadingIcon = { Icon(Icons.Filled.Security, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3949AB),
                    unfocusedBorderColor = Color(0xFF3949AB).copy(alpha = 0.5f)
                )
            )

            Button(
                onClick = onVerificar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = uiState.tokenValido && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3949AB)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Verificar código")
                }
            }

            Button(
                onClick = onAtras,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3949AB).copy(alpha = 0.3f)
                )
            ) {
                Text("Atrás", color = Color.White)
            }
        }
    }

    @Composable
    private fun ConfirmacionContrasenaStep(
    uiState: com.example.proyecto.ui.viewmodel.ForgotPasswordUiState,
    onNuevaContrasenaChange: (String) -> Unit,
    onConfirmarContrasenaChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onAtras: () -> Unit,
    viewModel: ForgotPasswordViewModel
) {
    var mostrarContrasena by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
            Text(
                "Nueva contraseña",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                "Ingresa una contraseña segura para tu cuenta.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            if (uiState.error != null) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color(0xFFCF6679).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = Color(0xFFCF6679),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            uiState.error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCF6679),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Requisitos de contraseña
            PasswordRequirementsCard(
                contraseña = uiState.nuevaContrasena
            )

            OutlinedTextField(
                value = uiState.nuevaContrasena,
                onValueChange = onNuevaContrasenaChange,
                label = { Text("Nueva contraseña") },
                placeholder = { Text("••••••••") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                        Icon(
                            if (mostrarContrasena) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3949AB),
                    unfocusedBorderColor = Color(0xFF3949AB).copy(alpha = 0.5f)
                )
            )

            OutlinedTextField(
                value = uiState.confirmarContrasena,
                onValueChange = onConfirmarContrasenaChange,
                label = { Text("Confirmar contraseña") },
                placeholder = { Text("••••••••") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { mostrarConfirmacion = !mostrarConfirmacion }) {
                        Icon(
                            if (mostrarConfirmacion) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (mostrarConfirmacion) VisualTransformation.None else PasswordVisualTransformation(),
                isError = uiState.confirmarContrasena.isNotEmpty() && !uiState.contraseniasCoinciden,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3949AB),
                    unfocusedBorderColor = Color(0xFF3949AB).copy(alpha = 0.5f)
                )
            )

            Button(
                onClick = onConfirmar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = uiState.contrasenaValida && uiState.contraseniasCoinciden && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA5)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Cambiar contraseña")
                }
            }

            Button(
                onClick = onAtras,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3949AB).copy(alpha = 0.3f)
                )
            ) {
                Text("Atrás", color = Color.White)
            }
        }
    }

    @Composable
    private fun ProgressIndicatorSteps(
    currentStep: Int,
    totalSteps: Int,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            color = if (index < currentStep) Color(0xFF00BFA5) else Color(0xFF3949AB),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            labels.forEachIndexed { index, label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index < currentStep) Color(0xFF00BFA5) else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PasswordRequirementsCard(
    contraseña: String
) {
    val tieneLength = contraseña.length >= 8
    val tieneMayuscula = contraseña.any { it.isUpperCase() }
    val tieneMinuscula = contraseña.any { it.isLowerCase() }
    val tieneNumero = contraseña.any { it.isDigit() }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF1E1E2E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RequirementRow("Al menos 8 caracteres", tieneLength)
            RequirementRow("Una mayúscula (A-Z)", tieneMayuscula)
            RequirementRow("Una minúscula (a-z)", tieneMinuscula)
            RequirementRow("Un número (0-9)", tieneNumero)
        }
    }
}

@Composable
private fun RequirementRow(
    texto: String,
    cumplido: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (cumplido) Icons.Filled.CheckCircle else Icons.Filled.Circle,
            contentDescription = null,
            tint = if (cumplido) Color(0xFF00BFA5) else Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp)
        )
        Text(
            texto,
            style = MaterialTheme.typography.labelSmall,
            color = if (cumplido) Color(0xFF00BFA5) else Color.White.copy(alpha = 0.5f)
        )
    }
}
