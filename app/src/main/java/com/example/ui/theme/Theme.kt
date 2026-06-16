package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Forced false to ensure the app always opens beautifully in Light Mode
    dynamicColor: Boolean = false, // Set to false to enforce our beautiful custom branding
    content: @Composable () -> Unit,
) {
    val colorScheme = LightColorScheme // Forced LightColorScheme for clean aesthetic


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
