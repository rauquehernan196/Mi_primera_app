package com.example.miprimeraapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkIoTColorScheme = darkColorScheme(
    primary = IoTNeonGreen,
    secondary = IoTForestGreen,
    tertiary = IoTWarningYellow,
    background = IoTBackground,
    surface = IoTCardSurface,
    surfaceVariant = IoTCardSurfaceVariant,
    onPrimary = IoTBackground,
    onSecondary = IoTTextPrimary,
    onBackground = IoTTextPrimary,
    onSurface = IoTTextPrimary,
    onSurfaceVariant = IoTTextSecondary,
    error = IoTErrorRed,
)

private val LightIoTColorScheme = lightColorScheme(
    primary = IoTForestGreen,
    secondary = IoTNeonGreen,
    tertiary = IoTWarningYellow,
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9ECEF),
    onPrimary = Color.White,
    onSecondary = Color(0xFF0E1210),
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
    error = IoTErrorRed,
)

@Composable
fun MiPrimeraAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkIoTColorScheme else LightIoTColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
