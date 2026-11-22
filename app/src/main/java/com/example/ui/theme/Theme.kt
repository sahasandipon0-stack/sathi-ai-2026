package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkTealPrimary,
    onPrimary = DarkOnTealPrimary,
    primaryContainer = DarkTealPrimaryContainer,
    onPrimaryContainer = DarkOnTealPrimaryContainer,
    secondary = DarkRoseSecondary,
    onSecondary = DarkOnRoseSecondary,
    secondaryContainer = DarkRoseSecondaryContainer,
    onSecondaryContainer = DarkOnRoseSecondaryContainer,
    tertiary = DarkMarigoldTertiary,
    onTertiary = DarkOnMarigoldTertiary,
    tertiaryContainer = DarkMarigoldTertiaryContainer,
    onTertiaryContainer = DarkOnMarigoldTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = OnTealPrimary,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = OnTealPrimaryContainer,
    secondary = RoseSecondary,
    onSecondary = OnRoseSecondary,
    secondaryContainer = RoseSecondaryContainer,
    onSecondaryContainer = OnRoseSecondaryContainer,
    tertiary = MarigoldTertiary,
    onTertiary = OnMarigoldTertiary,
    tertiaryContainer = MarigoldTertiaryContainer,
    onTertiaryContainer = OnMarigoldTertiaryContainer,
    background = CreamBackground,
    onBackground = OnCreamBackground,
    surface = CreamSurface,
    onSurface = OnCreamSurface,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = OnCreamSurfaceVariant,
    surfaceContainerLowest = CreamSurfaceContainerLowest,
    surfaceContainerLow = CreamSurfaceContainerLow,
    surfaceContainer = CreamSurfaceContainer,
    surfaceContainerHigh = CreamSurfaceContainerHigh,
    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant
)

val SathiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun SathiAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        shapes = SathiShapes,
        content = content
    )
}
