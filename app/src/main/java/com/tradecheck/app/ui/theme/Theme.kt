package com.tradecheck.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = DarkBg,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = TextPrimary,
    secondary = EmeraldLight,
    onSecondary = DarkBg,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = CardBorder,
    onSurfaceVariant = TextSecondary,
    error = RoseError,
    onError = TextPrimary
)

@Composable
fun TradeCheckTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
