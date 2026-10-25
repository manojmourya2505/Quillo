package com.inouiw.quillo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val QuilloColors = lightColorScheme(
    primary = QInk,
    onPrimary = QCard,
    secondary = QGreenInk,
    onSecondary = QCard,
    primaryContainer = QMint,
    onPrimaryContainer = QGreenInk,
    background = QSurface,
    onBackground = QInk,
    surface = QCard,
    onSurface = QInk,
    surfaceVariant = QTonal,
    onSurfaceVariant = QMuted,
    outline = QOutline,
)

@Composable
fun QuilloTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = QuilloColors,
        typography = QuilloTypography,
        content = content,
    )
}
