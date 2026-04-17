package com.example.proyecto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TermsScreen(navController: NavController) {
    Scaffold(
        containerColor = Color(0xFF0D0D1A),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Términos y Condiciones", color = Color.White, fontWeight = FontWeight.Bold) },
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
            Text("Última actualización: Abril 2026",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f))

            TermsSection("1. Aceptación de los Términos",
                "Al acceder y utilizar LexSign, usted acepta quedar vinculado por estos Términos y Condiciones. Si no está de acuerdo con alguna parte de estos términos, no podrá acceder al servicio.")

            TermsSection("2. Descripción del Servicio",
                "LexSign es una plataforma digital que conecta a usuarios con abogados certificados en Colombia. LexSign actúa como intermediario y no es responsable de los servicios legales prestados por los abogados inscritos en la plataforma.")

            TermsSection("3. Privacidad y Protección de Datos",
                "El tratamiento de sus datos personales se realiza conforme a la Ley 1581 de 2012 (Protección de Datos Personales) y el Decreto 1377 de 2013 de Colombia. Sus datos son almacenados de forma segura y no serán compartidos con terceros sin su consentimiento previo.")

            TermsSection("4. Uso Aceptable",
                "Usted se compromete a utilizar LexSign únicamente para fines legales. Está prohibido: (a) subir contenido ilegal o difamatorio; (b) intentar acceder a cuentas de otros usuarios; (c) usar la plataforma para actividades fraudulentas.")

            TermsSection("5. Documentos y Firma Digital",
                "Los documentos firmados digitalmente a través de LexSign tienen validez legal conforme a la Ley 527 de 1999 de Colombia. El usuario es responsable de verificar el contenido antes de firmar.")

            TermsSection("6. Responsabilidad Limitada",
                "LexSign no garantiza la disponibilidad ininterrumpida del servicio. En ningún caso LexSign será responsable por daños indirectos, incidentales o consecuentes derivados del uso de la plataforma.")

            TermsSection("7. Modificaciones",
                "LexSign se reserva el derecho de modificar estos términos en cualquier momento. Los cambios entrarán en vigor 30 días después de su publicación. El uso continuado del servicio implica la aceptación de los nuevos términos.")

            TermsSection("8. Contacto",
                "Para preguntas sobre estos términos, contacte a legal@lexsign.com")

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TermsSection(titulo: String, contenido: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = Color(0xFF3949AB))
            Text(contenido, style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f))
        }
    }
}
