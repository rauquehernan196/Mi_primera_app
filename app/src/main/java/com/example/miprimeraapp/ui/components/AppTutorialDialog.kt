package com.example.miprimeraapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class TutorialModuleStep(
    val title: String,
    val description: String,
    val emoji: String
)

@Composable
fun AppTutorialDialog(
    onFinish: () -> Unit
) {
    val steps = listOf(
        TutorialModuleStep(
            title = "1. Módulo de Autenticación",
            description = "Permite iniciar sesión con credenciales, registrar nuevas cuentas locales, recuperar contraseñas o autenticarse rápidamente mediante Google Sign-In de forma segura.",
            emoji = "🔑"
        ),
        TutorialModuleStep(
            title = "2. Módulo del Panel Principal",
            description = "Pantalla central (Home) desde donde se visualizan las operaciones principales de la plataforma, accesos directos y notificaciones del sistema.",
            emoji = "🏠"
        ),
        TutorialModuleStep(
            title = "3. Módulo de Perfil de Usuario",
            description = "Muestra la información de su cuenta, estado de sesión y verificación de cumplimiento conforme a la Ley N° 19.628 y la Ley Marco de Ciberseguridad de Chile.",
            emoji = "👤"
        ),
        TutorialModuleStep(
            title = "4. Módulo de Ajustes",
            description = "Permite personalizar la accesibilidad ajustando el tamaño de las letras y configurando el tema de la aplicación (Modo Oscuro, Claro o Automático del sistema).",
            emoji = "⚙️"
        )
    )

    var currentStep by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Skip Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onFinish) {
                        Text(
                            text = "Omitir",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Emoji Icon
                Text(
                    text = steps[currentStep].emoji,
                    fontSize = 56.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = steps[currentStep].title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                Text(
                    text = steps[currentStep].description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Step Indicators (Dots)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStep) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStep) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Anterior")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < steps.size - 1) {
                                currentStep++
                            } else {
                                onFinish()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (currentStep == steps.size - 1) "Comenzar" else "Siguiente")
                    }
                }
            }
        }
    }
}
