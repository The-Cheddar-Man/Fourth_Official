package com.example.fourthofficial.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FourthOfficialBlue,
    onPrimary = Color.White,

    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,

    secondary = DarkSecondary,
    onSecondary = Color.Black,

    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    background = DarkBackground,
    onBackground = DarkOnBackground,

    surface = DarkSurface,
    onSurface = DarkOnSurface,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,

    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,

    error = DestructiveRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = FourthOfficialBlue,
    onPrimary = Color.White,

    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,

    secondary = LightSecondary,
    onSecondary = Color.White,

    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,

    background = LightBackground,
    onBackground = LightOnBackground,

    surface = LightSurface,
    onSurface = LightOnSurface,

    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,

    outline = LightOutline,
    outlineVariant = LightOutlineVariant,

    error = DestructiveRed,
    onError = Color.White
)

@Composable
fun FourthOfficialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme =
            if (darkTheme) {
                DarkColorScheme
            } else {
                LightColorScheme
            },
        typography = Typography,
        shapes = FourthOfficialShapes,
        content = content
    )
}