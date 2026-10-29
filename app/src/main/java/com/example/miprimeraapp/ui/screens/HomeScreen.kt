package com.example.miprimeraapp.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miprimeraapp.ui.components.GeminiApiKeyDialog
import com.example.miprimeraapp.utils.GeminiAiService
import com.example.miprimeraapp.utils.VibrationUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EventLog(
    val id: Int,
    val time: String,
    val title: String,
    val description: String,
    val isCritical: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String,
    onLogout: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedBottomTab by remember { mutableIntStateOf(0) } // 0 = Dashboard, 1 = Aforo, 2 = Clima, 3 = Alertas
    val scrollState = rememberScrollState()

    // Interactive sensor states
    var aforoCount by remember { mutableIntStateOf(12) }
    var temperatureVal by remember { mutableFloatStateOf(21.5f) }
    val maxCapacity by remember { mutableFloatStateOf(15f) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var criticalDialogMessage by remember { mutableStateOf<String?>(null) }

    // Toggle states for Alertas tab
    var alertHeadsUp by remember { mutableStateOf(true) }
    var alertLockscreen by remember { mutableStateOf(true) }
    var alertAlarmSound by remember { mutableStateOf(true) }
    var alertCapacityExceeded by remember { mutableStateOf(true) }
    val alertThermalShock by remember { mutableStateOf(true) }

    // Gemini API states
    var geminiApiKey by remember { mutableStateOf("AIzaSyD-OSORNO-GEMINI-CLOUD-DEFAULT-KEY") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var isAnalyzingGemini by remember { mutableStateOf(false) }
    var geminiStatusMessage by remember { mutableStateOf<String?>(null) }

    val eventLogs = remember {
        mutableStateListOf(
            EventLog(1, "08:30", "Sistema Iniciado", "Sensores HC-SR04 A/B y SGP30 activos en Osorno.", false),
            EventLog(2, "09:15", "Aforo Normal", "Flujo bidireccional estable ($aforoCount / 15 pers).", false),
            EventLog(3, "10:00", "Verificación E2E", "Cumplimiento ISO 27400 y Ley N° 19.628 verificado.", false),
        )
    }

    // Trigger vibration and pop-up dialog when capacity limit (15) is reached
    LaunchedEffect(aforoCount) {
        if (aforoCount >= maxCapacity.toInt()) {
            VibrationUtils.vibrate(context, 1000)
            criticalDialogMessage = "🚨 ¡LÍMITE DE AFORO 25m² EXCEDIDO! Se han alcanzado $aforoCount de ${maxCapacity.toInt()} personas permitidas."
        }
    }

    // Trigger vibration and pop-up dialog when temperature is out of safe range
    LaunchedEffect(temperatureVal, alertThermalShock) {
        if (temperatureVal < 18f || temperatureVal > 28f) {
            VibrationUtils.vibrate(context, 1000)
            criticalDialogMessage = "🌡️ ¡ALERTA TÉRMICA CRÍTICA! Temperatura fuera de rango seguro: ${String.format(Locale.getDefault(), "%.1f", temperatureVal)} °C."
        }
    }

    fun runGeminiAnalysis(bitmap: Bitmap, apiKey: String) {
        capturedBitmap = bitmap
        isAnalyzingGemini = true
        geminiStatusMessage = "🤖 IA Reconociendo objetos y personas..."
        scope.launch {
            val result = GeminiAiService.recognizeObjectsAndPeopleInImage(bitmap, apiKey)
            isAnalyzingGemini = false
            if (result.isSuccess) {
                val description = result.getOrDefault("Objetos y personas reconocidas.")
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                geminiStatusMessage = "✅ IA Reconocimiento: $description"
                eventLogs.add(0, EventLog(eventLogs.size + 1, timeStr, "Gemini IA - Reconocimiento", description, false))
            } else {
                geminiStatusMessage = "❌ Error IA: ${result.exceptionOrNull()?.localizedMessage ?: "Fallo de red"}"
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            runGeminiAnalysis(it, geminiApiKey)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Alertas Auditoria ($username)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                ) {
                                    Text(
                                        text = " TLS 1.3 ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Text(
                                text = "🟢 EN LÍNEA • OSORNO NODE #04 (25 m²)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenProfile) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👤", fontSize = 16.sp)
                            }
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Text("⚙️", fontSize = 20.sp)
                    }
                    IconButton(onClick = onLogout) {
                        Text("🚪", fontSize = 20.sp)
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    icon = { Text("📊", fontSize = 20.sp) },
                    label = { Text("Dashboard") },
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1 },
                    icon = { Text("📡", fontSize = 20.sp) },
                    label = { Text("Aforo A/B") },
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2 },
                    icon = { Text("🌡️", fontSize = 20.sp) },
                    label = { Text("Aire SGP30") },
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    onClick = { selectedBottomTab = 3 },
                    icon = { Text("⚠️", fontSize = 20.sp) },
                    label = { Text("Alertas") },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (selectedBottomTab) {
                    0 -> {
                        // DASHBOARD TAB
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "📡 Conectividad Wi-Fi, Red & Bluetooth",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    ) {
                                        Text(
                                            text = " ACTIVO ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }
                                }

                                Text(
                                    text = "Estado de red Wi-Fi / Datos móviles, Bluetooth BLE y confirmación de permisos del sistema (Cámara, Ubicación, Almacenamiento, Notificaciones).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                Button(
                                    onClick = {
                                        val intent = Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null),
                                        )
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("🔐 Confirmar Permisos y Wi-Fi en Ajustes")
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "SISTEMA IoT 25 m² • ESTADO GENERAL",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)),
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = "AforoSense IoT • Sala 25 m²",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                            Text(
                                                text = "Tiempo Real",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "📊 Resumen: $aforoCount/15 personas | Temp: $temperatureVal°C",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // AFORO TAB
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "📡 Sistema Bidireccional HC-SR04 (A & B)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    ) {
                                        Text(
                                            text = " ${((aforoCount / maxCapacity) * 100).toInt()}% CAPACIDAD ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }
                                }

                                Text(
                                    text = "Puerta de Acceso (25 m²): Sensor A ➔ Sensor B (+1 Entrada) | Sensor B ➔ Sensor A (-1 Salida). Límite máx: 15 personas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(160.dp),
                                ) {
                                    CircularProgressIndicator(
                                        progress = { aforoCount / maxCapacity },
                                        modifier = Modifier.fillMaxSize(),
                                        strokeWidth = 14.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "SALA 25 m²",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline,
                                        )
                                        Text(
                                            text = "$aforoCount / ${maxCapacity.toInt()}",
                                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                        )
                                        Text(
                                            text = "${maxCapacity.toInt() - aforoCount} cupos libres",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Button(
                                        onClick = { if (aforoCount > 0) aforoCount-- },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text("🚪 Salida (B ➔ A)")
                                    }
                                    Button(
                                        onClick = { if (aforoCount < maxCapacity.toInt()) aforoCount++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text("🚪 Entrada (A ➔ B)")
                                    }
                                }
                            }
                        }

                        // AI Camera Module Card inside Aforo
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "📸 Reconocimiento IA (Gemini)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    )
                                    TextButton(onClick = { showApiKeyDialog = true }) {
                                        Text("🔑 API Key")
                                    }
                                }

                                Text(
                                    text = "La IA reconocerá y nombrará automáticamente personas y objetos en la habitación.",
                                    style = MaterialTheme.typography.bodySmall,
                                )

                                if (capturedBitmap != null) {
                                    Image(
                                        bitmap = capturedBitmap!!.asImageBitmap(),
                                        contentDescription = "Captured",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp),
                                        contentScale = ContentScale.Crop,
                                    )
                                }

                                geminiStatusMessage?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }

                                if (isAnalyzingGemini) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }

                                Button(
                                    onClick = { cameraLauncher.launch() },
                                    enabled = !isAnalyzingGemini,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(if (isAnalyzingGemini) "Analizando con Gemini IA..." else "📷 Tomar Foto y Analizar con IA")
                                }
                            }
                        }
                    }

                    2 -> {
                        // CLIMA / CALIDAD DEL AIRE TAB (MQ-135 / SGP30)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Text(
                                    text = "🌡️ CALIDAD DEL AIRE (SGP30 / MQ-135)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                )

                                Text(
                                    text = "Temperatura Actual: ${String.format(Locale.getDefault(), "%.1f", temperatureVal)} °C",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                )
                                Slider(
                                    value = temperatureVal,
                                    onValueChange = { temperatureVal = it },
                                    valueRange = 0f..40f,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    text = "Rango Operativo Óptimo: 18°C – 26°C",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    }

                    3 -> {
                        // ALERTAS TAB
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Text(
                                    text = "⚙️ Configuración Push & Pop-up",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                )
                                Text(
                                    text = "Control de entrega de notificaciones en segundo plano, daemon de alertas críticas y pasarela de audio.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                )

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Alertas Heads-up flotantes", fontWeight = FontWeight.SemiBold)
                                        Text("Banners superpuestos (Prioridad Máxima)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Switch(checked = alertHeadsUp, onCheckedChange = { alertHeadsUp = it })
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Pantalla bloqueada (Lockscreen)", fontWeight = FontWeight.SemiBold)
                                        Text("Muestra alerta con desglose de riesgo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Switch(checked = alertLockscreen, onCheckedChange = { alertLockscreen = it })
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Alarma Sonora CO₂ > 1,200 ppm", fontWeight = FontWeight.SemiBold)
                                        Text("Tono piezoeléctrico de 85dB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Switch(checked = alertAlarmSound, onCheckedChange = { alertAlarmSound = it })
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Alerta Aforo Excedido (HC-SR04 A/B)", fontWeight = FontWeight.SemiBold)
                                        Text("Notifica cuando supera la capacidad de 25 m²", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Switch(checked = alertCapacityExceeded, onCheckedChange = { alertCapacityExceeded = it })
                                }
                            }
                        }
                    }
                }
            }

            if (showApiKeyDialog) {
                GeminiApiKeyDialog(
                    initialKey = geminiApiKey,
                    onDismiss = { showApiKeyDialog = false },
                    onSaveKey = { newKey ->
                        geminiApiKey = newKey
                        showApiKeyDialog = false
                    },
                )
            }

            if (criticalDialogMessage != null) {
                AlertDialog(
                    onDismissRequest = { criticalDialogMessage = null },
                    title = { Text("🚨 ALERTA CRÍTICA DE SENSOR") },
                    text = { Text(criticalDialogMessage!!) },
                    confirmButton = {
                        Button(onClick = { criticalDialogMessage = null }) {
                            Text("Entendido / Silenciar")
                        }
                    },
                )
            }
        }
    }
}
