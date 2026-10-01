package com.example.miprimeraapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class AppThemeMode {
    LIGHT, DARK, AUTO
}

enum class FontSizeScale {
    SMALL, MEDIUM, LARGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: AppThemeMode,
    onThemeChanged: (AppThemeMode) -> Unit,
    currentFontSize: FontSizeScale,
    onFontSizeChanged: (FontSizeScale) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes de la Aplicación") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Volver", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tema de la Aplicación",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentTheme == AppThemeMode.LIGHT,
                            onClick = { onThemeChanged(AppThemeMode.LIGHT) },
                            label = { Text("Claro") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentTheme == AppThemeMode.DARK,
                            onClick = { onThemeChanged(AppThemeMode.DARK) },
                            label = { Text("Oscuro") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentTheme == AppThemeMode.AUTO,
                            onClick = { onThemeChanged(AppThemeMode.AUTO) },
                            label = { Text("Automático") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Font Size Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tamaño de las Letras (Accesibilidad)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentFontSize == FontSizeScale.SMALL,
                            onClick = { onFontSizeChanged(FontSizeScale.SMALL) },
                            label = { Text("Pequeña") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentFontSize == FontSizeScale.MEDIUM,
                            onClick = { onFontSizeChanged(FontSizeScale.MEDIUM) },
                            label = { Text("Mediana") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentFontSize == FontSizeScale.LARGE,
                            onClick = { onFontSizeChanged(FontSizeScale.LARGE) },
                            label = { Text("Grande") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
