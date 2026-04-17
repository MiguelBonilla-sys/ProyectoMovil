package com.example.proyecto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private data class FaqItem(val pregunta: String, val respuesta: String)

private val FAQ = listOf(
    FaqItem("¿Cómo creo una consulta?",
        "Ve a la pestaña Consultas y toca el botón '+'. Selecciona el área de práctica, describe tu caso y presiona Crear. También puedes crear una consulta directamente desde el perfil de un abogado en la pestaña Abogados."),
    FaqItem("¿Cómo me comunico con un abogado?",
        "Una vez creada la consulta, el abogado asignado podrá ver tu caso. Puedes enviar documentos adjuntos desde la pestaña Documentos, asociándolos a tu consulta."),
    FaqItem("¿Cómo subo un documento?",
        "Ve a la pestaña Documentos y toca el botón '+'. Puedes crear un documento nuevo con una plantilla legal predefinida o subir un archivo propio. El documento quedará asociado a tu cuenta."),
    FaqItem("¿Cómo firmo un documento?",
        "En la pestaña Documentos verás los documentos pendientes de firma. Toca el botón 'Firmar' en el documento correspondiente para registrar tu firma digital."),
    FaqItem("¿Mis datos están seguros?",
        "Sí. LexSign utiliza cifrado PBKDF2 para las contraseñas y Supabase con Row Level Security para proteger tus datos. Ningún otro usuario puede ver tu información."),
    FaqItem("¿Cómo cambio mi contraseña?",
        "Ve a Perfil > Seguridad > Cambiar contraseña. Deberás ingresar tu contraseña actual y la nueva."),
    FaqItem("¿Cómo elimino mi cuenta?",
        "En la sección Perfil, al final de la pantalla encontrarás la opción 'Eliminar mi cuenta'. Esta acción es permanente e irreversible."),
    FaqItem("¿Qué hago si olvido mi contraseña?",
        "Por el momento, contacta al administrador del sistema en admin@lexsign.com para restablecer tu contraseña."),
)

@Composable
fun HelpCenterScreen(navController: NavController) {
    Scaffold(
        containerColor = Color(0xFF0D0D1A),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Centro de Ayuda", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E2E))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Preguntas Frecuentes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(FAQ) { item ->
                FaqCard(item)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3949AB).copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("¿Necesitas más ayuda?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3949AB))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Escríbenos a soporte@lexsign.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqCard(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.pregunta,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF3949AB)
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(item.respuesta,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f))
            }
        }
    }
}
