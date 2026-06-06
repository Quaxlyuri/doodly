package com.doodly.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val ColorWhite = androidx.compose.ui.graphics.Color(0xFFF5F7FA)
private val ColorMuted = androidx.compose.ui.graphics.Color(0xFFAAB2BD)

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Surface,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = Danger,
    outline = Divider
)

private val DarkColors = darkColorScheme(
    primary = Primary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = ColorWhite,
    onSurface = ColorWhite,
    onSurfaceVariant = ColorMuted,
    error = Danger
)

private val DoodlyShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun DoodlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = DoodlyTypography,
        shapes = DoodlyShapes,
        content = content
    )
}
