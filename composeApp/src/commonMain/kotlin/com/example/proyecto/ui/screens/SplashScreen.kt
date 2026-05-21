package com.example.proyecto.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.data.session.SessionManager
import com.example.proyecto.navigation.Routes
import com.example.proyecto.ui.components.LexSignLogo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var loadingStatus by remember { mutableStateOf("Iniciando...") }
    var targetProgress by remember { mutableStateOf(0f) }
    var targetScale by remember { mutableStateOf(0.4f) }
    var targetAlpha by remember { mutableStateOf(0f) }

    val logoScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "logoScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 700),
        label = "contentAlpha"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        targetScale = 1f
        targetAlpha = 1f

        delay(600)
        targetProgress = 0.35f
        loadingStatus = "Cargando recursos..."

        delay(700)
        targetProgress = 0.65f
        loadingStatus = "Verificando sesión..."

        val sessionRestored = SessionManager.restoreSession()

        delay(500)
        targetProgress = 1f
        loadingStatus = if (sessionRestored) "Bienvenido de vuelta" else "Listo"

        delay(600)

        val destination = if (sessionRestored) Routes.MAIN else Routes.WELCOME
        navController.navigate(destination) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A237E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        ) {
            // Logo con animación de escala y rebote
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(logoScale)
                    .clip(CircleShape)
                    .background(Color(0xFF3949AB)),
                contentAlignment = Alignment.Center
            ) {
                LexSignLogo(
                    modifier = Modifier.size(72.dp),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "LexSign",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = contentAlpha)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Asesoría Legal Digital",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = contentAlpha * 0.75f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Barra de progreso con estados de carga
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF7986CB),
                trackColor = Color.White.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = loadingStatus,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = contentAlpha * 0.65f),
                textAlign = TextAlign.Center
            )
        }

        // Versión en la parte inferior
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 36.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}
