package com.example.miprimeraapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppLogoIcon(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 96.dp
) {
    val purpleBorderColor = Color(0xFF7E57C2) // Borde Morado / Purple border
    val greenColor = Color(0xFF4CAF50)        // Verde / Green
    val yellowColor = Color(0xFFFFEB3B)       // Amarillo / Yellow
    val redColor = Color(0xFFF44336)          // Rojo / Red

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1B4B) // Fondo Indigo Oscuro
        ),
        modifier = modifier
            .size(sizeDp)
            .border(
                width = 3.5.dp,
                color = purpleBorderColor,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Anillos de color Verde, Amarillo y Rojo
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            ) {
                val strokeWidth = 8f
                val diameter = size.minDimension - strokeWidth

                // Segmento 1: Verde (Seguridad)
                drawArc(
                    color = greenColor,
                    startAngle = 135f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth)
                )

                // Segmento 2: Amarillo (Advertencia)
                drawArc(
                    color = yellowColor,
                    startAngle = 235f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth)
                )

                // Segmento 3: Rojo (Alerta Crítica)
                drawArc(
                    color = redColor,
                    startAngle = 335f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth)
                )
            }

            // Iconos centrales de Personas (👥) + Temperatura (🌡️)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(2.dp)
            ) {
                Text(
                    text = "👥",
                    fontSize = (sizeDp.value * 0.32f).sp
                )
                Text(
                    text = "🌡️",
                    fontSize = (sizeDp.value * 0.32f).sp
                )
            }
        }
    }
}
