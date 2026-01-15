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
    primary = Color(0xFF7DBEDC),        // Soft sky blue (brand color)
    secondary = Color(0xFF25517B),      // Deep blue
    background = Color(0xFF2B2F2D),     // Slightly darker, cleaner base
    surface = Color(0xFF323634),        // Elevated surface
    onPrimary = Color(0xFF0E2A38),      // Dark text on light blue
    onSecondary = Color(0xFFEAF4FA),    // Light text on dark blue
    tertiary = Color(0xFF4FA3B8),     // Cool teal-blue accent
    onTertiary = Color(0xFF082A33) ,  // Dark text on tertiary
    onBackground = Color(0xFFE7ECEA),   // Soft white (not pure white)
    onSurface = Color(0xFFE7ECEA)
)


private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2F7FA6),      // Deeper blue version of your primary
    secondary = Color(0xFF5A8FB8),    // Softer blue (inverse of dark secondary)
    tertiary = Color(0xFF6FC2D1),
    background = Color(0xFFF3F6F4),   // Very light green-gray (inverse of #323634)
    surface = Color(0xFFFFFFFF),      // Clean white surface
    onPrimary = Color(0xFFFFFFFF),    // White text on primary
    onSecondary = Color(0xFF0F1F2A),
    onBackground = Color(0xFF1E2421), // Dark gray-green text
    onSurface = Color(0xFF1E2421),
    onTertiary = Color(0xFF0B2A33),
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