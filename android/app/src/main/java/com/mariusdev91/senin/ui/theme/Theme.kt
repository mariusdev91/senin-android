package com.mariusdev91.senin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Sky,
    onPrimary = Color.White,
    secondary = Coral,
    onSecondary = Color.White,
    background = Sand,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Ink,
    outline = Outline,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF74D2F6),
    onPrimary = Color(0xFF03222E),
    secondary = Color(0xFFFFB18B),
    onSecondary = Color(0xFF402313),
    background = Color(0xFF0D1B2A),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF112234),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1B334A),
    onSurfaceVariant = Color(0xFFD2DFEA),
    outline = Color(0xFF7FA2BA),
)

private val SeninShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

@Composable
fun SeninTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SeninTypography,
        shapes = SeninShapes,
        content = content,
    )
}
