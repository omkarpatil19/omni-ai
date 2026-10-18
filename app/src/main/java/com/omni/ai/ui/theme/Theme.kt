package com.omni.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val OmniBg = Color(0xFF05070F)
val OmniSurface = Color(0xFF0D1224)
val OmniCyan = Color(0xFF22E4FF)
val OmniViolet = Color(0xFF9D5CFF)
val OmniText = Color(0xFFE6F1FF)
val OmniMuted = Color(0xFF7D8BA8)
val OmniError = Color(0xFFFF5C7A)

private val DarkOmniColors = darkColorScheme(
    primary = OmniCyan,
    onPrimary = OmniBg,
    secondary = OmniViolet,
    background = OmniBg,
    onBackground = OmniText,
    surface = OmniSurface,
    onSurface = OmniText,
    error = OmniError,
)

@Composable
fun OmniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkOmniColors,
        content = content,
    )
}
