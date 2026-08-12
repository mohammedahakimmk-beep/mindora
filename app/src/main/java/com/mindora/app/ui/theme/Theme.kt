package com.mindora.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = NightSky,
    primaryContainer = DeepTeal,
    onPrimaryContainer = WarmSand,
    secondary = Ember,
    onSecondary = NightSky,
    secondaryContainer = EmberLight,
    onSecondaryContainer = NightSky,
    tertiary = StarGold,
    onTertiary = NightSky,
    background = NightSky,
    onBackground = WarmSand,
    surface = SurfaceDark,
    onSurface = WarmSand,
    surfaceVariant = DeepTeal,
    onSurfaceVariant = SandDark,
    error = ErrorRed,
    onError = NightSky
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = WarmSand,
    primaryContainer = TealLight,
    onPrimaryContainer = NightSky,
    secondary = Ember,
    onSecondary = WarmSand,
    secondaryContainer = EmberLight,
    onSecondaryContainer = NightSky,
    tertiary = StarGold,
    onTertiary = NightSky,
    background = WarmSand,
    onBackground = NightSky,
    surface = WarmSand,
    onSurface = NightSky,
    surfaceVariant = SandDark,
    onSurfaceVariant = DeepTeal,
    error = ErrorRed,
    onError = WarmSand
)

@Composable
fun MindoraTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MindoraTypography,
        content = content
    )
}
