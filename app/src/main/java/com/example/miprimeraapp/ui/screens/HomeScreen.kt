package com.example.miprimeraapp.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miprimeraapp.ui.components.AppLogoIcon
import com.example.miprimeraapp.ui.components.GeminiApiKeyDialog
import com.example.miprimeraapp.utils.GeminiAiService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EventLog(
    val id: Int,
    val time: String,
    val title: String,
    val description: String,
    val isCritical: Boolean
)

data class AlertMessage(
    val id: String,
    val title: String,
    val message: String,
    val isCritical: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String,
    onLogout: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedModuleIndex by remember { mutableIntStateOf(0) } // 0 = Todos, 1 = Aforo, 2 = Temp, 3 = Cámara, 4 = Historial
    val scrollState = rememberScrollState()

    val currentDateStr = remember {
        val localeEs = Locale.forLanguageTag("es-CL")
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", localeEs)
        sdf.format(Date()).replaceFirstChar { it.uppercase() }
    }

    // Interactive sensor states
    var aforoCount by remember { mutableIntStateOf(14) }
    var temperatureVal by remember { mutableFloatStateOf(21.5f) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Gemini API states
    var geminiApiKey by remember { mutableStateOf("AIzaSyD-OSORNO-GEMINI-CLOUD-DEFAULT-KEY") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var isAnalyzingGemini by remember { mutableStateOf(false) }
    var geminiStatusMessage by remember { mutableStateOf<String?>(null) }
    var pendingBitmapForAnalysis by remember { mutableStateOf<Bitmap?>(null) }

    val activeAlerts = remember { mutableStateListOf<AlertMessage>() }

    val eventLogs = remember {
        mutableStateListOf(
            EventLog(1, "08:30", "Sistema Iniciado", "Sensores HC-SR04 y temperatura activos en Osorno.", false),
            EventLog(2, "09:15", "Aforo Normal", "Flujo de personas dentro del rango permitido (14 pers).", false),
            EventLog(3, "10:00", "Verificación de Seguridad", "Cumplimiento Ley N° 19.628 y Ciberseguridad verificado.", false)
        )
    }

    fun runGeminiAnalysis(bitmap: Bitmap, apiKey: String) {
        capturedBitmap = bitmap
        isAnalyzingGemini = true
        geminiStatusMessage = "🤖 Gemini IA reconociendo personas y objetos..."
        scope.launch {
            val result = GeminiAiService.recognizeObjectsAndPeopleInImage(bitmap, apiKey)
            isAnalyzingGemini = false
            if (result.isSuccess) {
                val description = result.getOrDefault("Objetos y personas reconocidas.")
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                geminiStatusMessage = "✅ IA Reconocimiento: $description"
                eventLogs.add(0, EventLog(eventLogs.size + 1, timeStr, "Gemini IA - Reconocimiento", description, false))
            } else {
                geminiStatusMessage = "❌ Error Gemini IA: ${result.exceptionOrNull()?.localizedMessage ?: "Fallo de red"}"
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            runGeminiAnalysis(bitmap, geminiApiKey)
        }
    }

    // Dynamic threshold notifications trigger
    LaunchedEffect(aforoCount) {
        if (aforoCount >= 28) {
            if (activeAlerts.none { it.id == "aforo_alert" && it.isCritical }) {
                activeAlerts.removeAll { it.id == "aforo_alert" }
                activeAlerts.add(AlertMessage("aforo_alert", "🚨 Alerta Crítica de Aforo", "Se ha alcanzado el límite máximo ($aforoCount / 30 personas).", true))
            }
        } else if (aforoCount >= 25) {
            if (activeAlerts.none { it.id == "aforo_alert" }) {
                activeAlerts.add(AlertMessage("aforo_alert", "⚠️ Aviso de Aforo", "El aforo se está aproximando al límite ($aforoCount / 30 personas).", false))
            }
        } else {
            activeAlerts.removeAll { it.id == "aforo_alert" }
        }
    }

    LaunchedEffect(temperatureVal) {
        if (temperatureVal >= 36f) {
            if (activeAlerts.none { it.id == "temp_alert" && it.isCritical }) {
                activeAlerts.removeAll { it.id == "temp_alert" }
                activeAlerts.add(AlertMessage("temp_alert", "🔥 Alerta Crítica de Temperatura", "Temperatura ambiental muy alta (${String.format(Locale.getDefault(), "%.1f", temperatureVal)} °C).", true))
            }
        } else if (temperatureVal >= 30f) {
            if (activeAlerts.none { it.id == "temp_alert" }) {
                activeAlerts.add(AlertMessage("temp_alert", "⚠️ Aviso de Temperatura", "Temperatura ambiental elevada (${String.format(Locale.getDefault(), "%.1f", temperatureVal)} °C).", false))
            }
        } else {
            activeAlerts.removeAll { it.id == "temp_alert" }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
                    ) {
                        AppLogoIcon(sizeDp = 44.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "📱 Módulos Interactivos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Osorno • HC-SR04",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                    NavigationDrawerItem(
                        label = { Text("🏠 Ver Todos los Módulos", fontWeight = FontWeight.SemiBold) },
                        selected = selectedModuleIndex == 0,
                        onClick = {
                            selectedModuleIndex = 0
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("🤖 1. Control de Aforo HC-SR04", fontWeight = FontWeight.Medium) },
                        selected = selectedModuleIndex == 1,
                        onClick = {
                            selectedModuleIndex = 1
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("🌡️ 2. Calidad de Temperatura", fontWeight = FontWeight.Medium) },
                        selected = selectedModuleIndex == 2,
                        onClick = {
                            selectedModuleIndex = 2
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("📸 3. Reconocimiento por IA", fontWeight = FontWeight.Medium) },
                        selected = selectedModuleIndex == 3,
                        onClick = {
                            selectedModuleIndex = 3
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("📜 4. Historial de Alertas (Feed)", fontWeight = FontWeight.Medium) },
                        selected = selectedModuleIndex == 4,
                        onClick = {
                            selectedModuleIndex = 4
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = { Text("👤 Perfil de Usuario", fontWeight = FontWeight.Medium) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenProfile()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("⚙️ Ajustes", fontWeight = FontWeight.Medium) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenSettings()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    NavigationDrawerItem(
                        label = { Text("🚪 Cerrar Sesión", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Bar with AppLogoIcon and Hamburger Menu
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppLogoIcon(sizeDp = 48.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Hola, $username 👋",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentDateStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Text(text = "☰", fontSize = 24.sp)
                        }
                    }
                }

                // Active Dismissible Pop-up Alert Notifications
                activeAlerts.forEach { alert ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (alert.isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (alert.isCritical) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = alert.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (alert.isCritical) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            IconButton(
                                onClick = { activeAlerts.remove(alert) }
                            ) {
                                Text(text = "✕", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Module Content Display (Map module removed)
                if (selectedModuleIndex == 0 || selectedModuleIndex == 1) {
                    // Module 1: Control de Aforo HC-SR04
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🤖 1. Control de Aforo HC-SR04",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Personas detectadas en tiempo real: $aforoCount / 30 máx",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            LinearProgressIndicator(
                                progress = { aforoCount / 30f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = if (aforoCount > 25) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { if (aforoCount > 0) aforoCount-- },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("− Salida")
                                }
                                Button(
                                    onClick = { aforoCount++ },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+ Ingreso")
                                }
                            }
                        }
                    }
                }

                if (selectedModuleIndex == 0 || selectedModuleIndex == 2) {
                    // Module 2: Calidad de Temperatura
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🌡️ 2. Calidad de Temperatura Ambiental",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Temperatura actual: ${String.format(Locale.getDefault(), "%.1f", temperatureVal)} °C",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Slider(
                                value = temperatureVal,
                                onValueChange = { temperatureVal = it },
                                valueRange = 0f..40f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (selectedModuleIndex == 0 || selectedModuleIndex == 3) {
                    // Module 3: Reconocimiento de Objetos y Personas por IA
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📸 3. Reconocimiento por IA (Gemini)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                TextButton(onClick = { showApiKeyDialog = true }) {
                                    Text(if (geminiApiKey.isBlank()) "🔑 Configurar API Key" else "✅ API Key OK")
                                }
                            }

                            Text(
                                text = "Capture una foto. La IA de Gemini reconocerá y nombrará todas las personas y objetos presentes en la escena.",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (capturedBitmap != null) {
                                Image(
                                    bitmap = capturedBitmap!!.asImageBitmap(),
                                    contentDescription = "Captured Photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            geminiStatusMessage?.let { status ->
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (status.startsWith("❌")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }

                            if (isAnalyzingGemini) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }

                            Button(
                                onClick = { cameraLauncher.launch() },
                                enabled = !isAnalyzingGemini,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isAnalyzingGemini) "Reconociendo con Gemini IA..." else "📷 Tomar Foto y Reconocer con IA")
                            }
                        }
                    }
                }

                if (selectedModuleIndex == 0 || selectedModuleIndex == 4) {
                    // Module 4: Historial de Alertas (Feed)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📜 4. Historial de Alertas (Feed)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            eventLogs.forEach { log ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "[${log.time}] ${log.title}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = log.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }

                // Quick Navigation Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onOpenProfile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ver Perfil")
                    }

                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ajustes")
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
                        pendingBitmapForAnalysis?.let { bmp ->
                            runGeminiAnalysis(bmp, newKey)
                            pendingBitmapForAnalysis = null
                        }
                    }
                )
            }
        }
    }
}
