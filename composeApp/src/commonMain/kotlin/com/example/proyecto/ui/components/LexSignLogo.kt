package com.example.proyecto.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Balanza de justicia dibujada en código — logo oficial de LexSign.
 *
 * Geometría (espacio normalizado 0..1):
 *  - Adorno (círculo sólido) en la punta del eje central
 *  - Eje vertical central
 *  - Viga ligeramente inclinada con punto de pivote marcado
 *  - Dos cadenas y platos (borde recto + arco cóncavo)
 *  - Base + pies
 *
 * La inclinación de la viga (lado derecho más bajo) es intencional:
 * evoca una balanza en movimiento, no en reposo perfecto.
 */
@Composable
fun LexSignLogo(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        val sw  = w * 0.055f   // trazo principal
        val csw = w * 0.033f   // cadenas (más delgadas)

        fun mainStroke() = Stroke(width = sw,  cap = StrokeCap.Round, join = StrokeJoin.Round)

        // ── Adorno en la punta del eje ──────────────────────────────
        val ornR  = w * 0.055f
        val ornCY = h * 0.075f
        drawCircle(color = color, radius = ornR, center = Offset(cx, ornCY))

        // ── Eje vertical ────────────────────────────────────────────
        drawLine(
            color = color,
            start = Offset(cx, ornCY + ornR),
            end   = Offset(cx, h * 0.80f),
            strokeWidth = sw,
            cap = StrokeCap.Round
        )

        // ── Viga (ligeramente inclinada: derecha más baja) ──────────
        val bLX = w * 0.15f;  val bLY = h * 0.265f
        val bRX = w * 0.85f;  val bRY = h * 0.305f
        drawLine(
            color = color,
            start = Offset(bLX, bLY),
            end   = Offset(bRX, bRY),
            strokeWidth = sw,
            cap = StrokeCap.Round
        )

        // Punto de pivote en la intersección viga–eje
        val pivotY = bLY + (bRY - bLY) * ((cx - bLX) / (bRX - bLX))
        drawCircle(color = color, radius = sw * 0.85f, center = Offset(cx, pivotY))

        // ── Geometría de los platos ──────────────────────────────────
        val pHW = w * 0.115f   // semiancho de cada plato
        val pD  = h * 0.078f   // profundidad del cuenco

        // ── Plato izquierdo (lado alto) ──────────────────────────────
        val lpY = h * 0.545f
        // cadena
        drawLine(color = color, start = Offset(bLX, bLY), end = Offset(bLX, lpY),
            strokeWidth = csw, cap = StrokeCap.Round)
        // borde superior del plato
        drawLine(color = color,
            start = Offset(bLX - pHW, lpY), end = Offset(bLX + pHW, lpY),
            strokeWidth = sw, cap = StrokeCap.Round)
        // cuenco — arco inferior de la elipse inscrita en el rectángulo
        // startAngle=0 (3 h) → sweepAngle=180° CW → pasa por 6 h (punto más bajo) → termina en 9 h (borde izq)
        drawArc(
            color = color,
            startAngle = 0f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(bLX - pHW, lpY - pD),
            size = Size(pHW * 2f, pD * 2f),
            style = mainStroke()
        )

        // ── Plato derecho (lado bajo) ─────────────────────────────────
        val rpY = h * 0.615f
        // cadena
        drawLine(color = color, start = Offset(bRX, bRY), end = Offset(bRX, rpY),
            strokeWidth = csw, cap = StrokeCap.Round)
        // borde superior del plato
        drawLine(color = color,
            start = Offset(bRX - pHW, rpY), end = Offset(bRX + pHW, rpY),
            strokeWidth = sw, cap = StrokeCap.Round)
        // cuenco
        drawArc(
            color = color,
            startAngle = 0f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(bRX - pHW, rpY - pD),
            size = Size(pHW * 2f, pD * 2f),
            style = mainStroke()
        )

        // ── Base ─────────────────────────────────────────────────────
        drawLine(
            color = color,
            start = Offset(cx - w * 0.13f, h * 0.82f),
            end   = Offset(cx + w * 0.13f, h * 0.82f),
            strokeWidth = sw * 1.1f,
            cap = StrokeCap.Round
        )

        // ── Pies (barra más ancha) ────────────────────────────────────
        drawLine(
            color = color,
            start = Offset(cx - w * 0.195f, h * 0.905f),
            end   = Offset(cx + w * 0.195f, h * 0.905f),
            strokeWidth = sw * 1.25f,
            cap = StrokeCap.Round
        )
    }
}
