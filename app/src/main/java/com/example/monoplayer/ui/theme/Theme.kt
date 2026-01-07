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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Define your custom colors if not already defined
// Your specific colors
val fairywhite = Color(0xFFEDF1F4)
val customDark = Color(0xFF212226)


private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7DBEDC),
    secondary = Color(0xFF25517B),
    tertiary = Purple80,
    surface = customDark,      // Dark background
    onPrimary = fairywhite,    // Light text
    onSurface = fairywhite,
    background = Color(0xFF323634)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7DBEDC),
    secondary = Color(0xFF25517B),
    tertiary = Purple40,
    surface = fairywhite,      // Light background (Reverted)
    onPrimary = customDark,    // Dark text (Reverted)
    onSurface = customDark     // Ensures text on background is dark
)

@Composable
fun MonoPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // This picks the scheme based on the boolean
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}