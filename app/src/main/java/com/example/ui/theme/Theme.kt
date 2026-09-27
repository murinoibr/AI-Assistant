package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ModernDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = NeonBlue,
    onSecondary = Color.White,
    tertiary = ElectricViolet,
    background = BackgroundObsidian,
    onBackground = TextPureWhite,
    surface = SurfaceSpace,
    onSurface = TextPureWhite,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextMuted,
    outline = BorderSubtle
)

@Composable
fun VozIATheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ModernDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    VozIATheme(darkTheme = true, content = content)
}
