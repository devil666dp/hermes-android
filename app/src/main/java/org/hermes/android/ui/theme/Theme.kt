package org.hermes.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HermesGold,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4A3700),
    onPrimaryContainer = HermesLightGold,
    secondary = HermesBronze,
    onSecondary = Color.Black,
    surface = DarkSurface,
    onSurface = Color(0xFFEEEEEE),
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCCCCCC),
    background = Color(0xFF0F0F0F),
    onBackground = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = HermesDarkGold,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF3CD),
    onPrimaryContainer = Color(0xFF4A3700),
    secondary = HermesBronze,
    onSecondary = Color.White,
    surface = LightSurface,
    onSurface = Color(0xFF1E1E1E),
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF444444),
    background = Color.White,
    onBackground = Color.Black
)

@Composable
fun HermesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
