package com.example.proyecto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LexSignDarkColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoDark,
    onPrimaryContainer = Color.White,
    secondary = TealAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color.White,
    tertiary = IndigoLight,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    surfaceVariant = Color(0xFF2A2A3C),
    outline = Color(0xFF424242),
    error = ErrorRed,
    onError = Color.Black
)

@Composable
fun LexSignTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LexSignDarkColorScheme,
        typography = LexSignTypography,
        content = content
    )
}
