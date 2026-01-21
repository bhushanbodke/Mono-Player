package com.example.monoplayer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.monoplayer.MyViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



// Define your custom colors if not already defined
// Your specific colors
val fairywhite = Color(0xFFEDF1F4)
val customDark = Color(0xFF212226)


private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7DBEDC),
    secondary = Color(0xFF4FA3B8),     // Teal blue accent
    background = Color(0xFF1A1C1E),    // Deeper, darker background
    surface = Color(0xFF2C2C2C),       // Lighter surface for cards
    onPrimary = Color(0xFF003544),
    onSurface = Color(0xFFE2E2E6),
    onSurfaceVariant = Color(0xFFC2C7CE) // For "videos count" and "size"
)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2F7FA6),
    secondary = Color(0xFF5A8FB8),
    tertiary = Color(0xFF4FA3B8),
    background = Color(0xFFFBFDFD),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),    // White text on the blue brand color
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
    onSurfaceVariant = Color(0xFF535F67)
)


@Composable
fun MonoPlayerTheme(
    vm: MyViewModel,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // This picks the scheme based on the boolean
    val isLightMode by vm.isLightMode.collectAsState()

    val colorScheme = if (!isLightMode) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}