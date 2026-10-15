package com.example.miprimeraapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ConsentAndSecurityDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    var acceptedTerms by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.925f)
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppLogoIcon(sizeDp = 64.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Consentimiento y Seguridad de Datos",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Cumplimiento Ley N° 19.628 (Protección de Datos) y Ley Marco de Ciberseguridad N° 21.663 (Chile)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Content with Terms and Permission justifications
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Estimado usuario, para brindarle la mejor experiencia y cumplir con los más altos estándares de ciberseguridad y protección de datos personales vigentes en Chile, requerimos su consentimiento informado y la autorización de los siguientes permisos y accesos:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    PermissionDetailItem(
                        title = "🌐 Conexión a Internet y Red",
                        description = "Permite la comunicación segura con servicios en la nube y la API de Gemini para el reconocimiento de objetos e inteligencia artificial con transmisión cifrada."
                    )

                    PermissionDetailItem(
                        title = "📷 Cámara",
                        description = "Utilizado exclusivamente para la captura de imágenes o documentos requeridos dentro de la funcionalidad de la aplicación. Ninguna imagen se comparte sin su consentimiento."
                    )

                    PermissionDetailItem(
                        title = "📍 Ubicación",
                        description = "Permite acceder a su ubicación geográfica (precisa o aproximada) para ofrecerle servicios y registros basados en su posición actual dentro de la plataforma."
                    )

                    PermissionDetailItem(
                        title = "💾 Almacenamiento",
                        description = "Necesario para guardar archivos temporales, preferencias y registros locales de forma segura en su dispositivo."
                    )

                    PermissionDetailItem(
                        title = "🔔 Notificaciones",
                        description = "Para enviarle avisos importantes, alertas de seguridad, actualizaciones de estado y notificaciones del sistema."
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Términos de Seguridad y Privacidad:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "1. Tratamiento de Datos: Sus datos personales son tratados con estricta confidencialidad conforme a la Ley N° 19.628 sobre Protección de la Vida Privada.\n\n" +
                                "2. Estándares de Ciberseguridad: La aplicación implementa controles técnicos y organizativos alineados con la Ley Marco de Ciberseguridad (Ley N° 21.663) para prevenir incidentes de seguridad y proteger la integridad de la información.\n\n" +
                                "3. Derechos ARCO: Usted tiene derecho en todo momento a solicitar el Acceso, Rectificación, Cancelación u Oposición (Derechos ARCO) de sus datos personales comunicándose con nuestro soporte técnico.\n\n" +
                                "4. Revocación: Puede revocar los permisos otorgados en cualquier momento desde la configuración de su dispositivo Android.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Checkbox for Acceptance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it }
                    )
                    Text(
                        text = "He leído y acepto los Términos de Seguridad, Política de Privacidad y el uso de los permisos y accesos solicitados.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rechazar")
                    }

                    Button(
                        onClick = onAccept,
                        enabled = acceptedTerms,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aceptar y Continuar")
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionDetailItem(title: String, description: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
