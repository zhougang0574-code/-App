package com.example.readapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = SunYellow,
    onPrimary = DeepNavy,
    secondary = SkyBlue,
    onSecondary = DeepNavy,
    tertiary = MintGreen,
    background = WarmCream,
    onBackground = DeepNavy,
    surface = SoftWhite,
    onSurface = DeepNavy,
    error = WrongRed,
)

@Composable
fun KidsMathTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
