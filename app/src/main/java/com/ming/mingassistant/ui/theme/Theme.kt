package com.ming.mingassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BilibiliBlue = Color(0xFF00A1D6)
val BilibiliPink = Color(0xFFFB7299)
private val BilibiliBlueDark = Color(0xFF0E99C9)

private val LightColors = lightColorScheme(
    primary = BilibiliBlue,
    onPrimary = Color.White,
    secondary = BilibiliPink,
    onSecondary = Color.White,
    tertiary = BilibiliBlueDark,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FC8E8),
    onPrimary = Color(0xFF002A38),
    secondary = BilibiliPink,
    onSecondary = Color.White,
)

@Composable
fun MingAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}