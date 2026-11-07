package com.example.pilahin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    background = SplashBackground,
    surface = SplashBackground,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun PilahinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}