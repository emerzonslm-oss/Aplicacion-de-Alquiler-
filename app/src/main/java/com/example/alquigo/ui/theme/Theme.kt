package com.example.alquigo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.LocalHazeStyle

private val LightColorScheme = lightColorScheme(
    primary = AirbnbPrimary,
    secondary = AirbnbActive,
    tertiary = AirbnbGradientStart,
    background = BackgroundWhite,
    surface = BackgroundWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderLight
)

val GlobalHazeStyle = HazeStyle(
    backgroundColor = Color.White,
    tint = HazeTint(Color.White.copy(alpha = 0.65f)),
    blurRadius = 20.dp,
    noiseFactor = 0f
)

@Composable
fun AlquiGoTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalHazeStyle provides GlobalHazeStyle
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
