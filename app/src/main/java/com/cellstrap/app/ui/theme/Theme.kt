package com.cellstrap.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cellstrap brand colors — neon green on black
val CellGreen = Color(0xFF39FF14)
val CellGreenDim = Color(0xFF2BC210)
val CellGreenDark = Color(0xFF1A7A0A)
val CellGreenGlow = Color(0xFF7FFF5B)
val CellBlack = Color(0xFF050505)
val CellSurface = Color(0xFF0D0D0D)
val CellCard = Color(0xFF111111)
val CellCardBorder = Color(0xFF1C2B1A)
val CellDivider = Color(0xFF1E2E1C)
val CellTextPrimary = Color(0xFFE8FFE5)
val CellTextSecondary = Color(0xFF7AAB75)
val CellTextMuted = Color(0xFF3D5C3A)
val CellError = Color(0xFFFF4444)
val CellWarning = Color(0xFFFFAA00)
val CellSuccess = Color(0xFF39FF14)

private val DarkColorScheme = darkColorScheme(
    primary = CellGreen,
    onPrimary = CellBlack,
    primaryContainer = CellGreenDark,
    onPrimaryContainer = CellGreenGlow,
    secondary = CellGreenDim,
    onSecondary = CellBlack,
    secondaryContainer = Color(0xFF0A1A08),
    onSecondaryContainer = CellGreenGlow,
    tertiary = CellGreenGlow,
    onTertiary = CellBlack,
    background = CellBlack,
    onBackground = CellTextPrimary,
    surface = CellSurface,
    onSurface = CellTextPrimary,
    surfaceVariant = CellCard,
    onSurfaceVariant = CellTextSecondary,
    outline = CellCardBorder,
    outlineVariant = CellDivider,
    error = CellError,
    onError = Color.White,
    errorContainer = Color(0xFF3B0000),
    onErrorContainer = CellError,
)

@Composable
fun CellstrapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = CellstrapTypography,
        content = content
    )
}
