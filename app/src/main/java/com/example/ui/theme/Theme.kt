package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NightGreen,
    onPrimary = Color(0xFF042817),
    primaryContainer = IslamicGreenDark,
    onPrimaryContainer = Color(0xFFB8F3D1),
    secondary = NightGold,
    onSecondary = Color(0xFF433003),
    secondaryContainer = Color(0xFF5E4608),
    onSecondaryContainer = Color(0xFFFDE8AC),
    background = NightDarkBg,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightTextSecondary,
    outline = Color(0xFF3B444E)
)

private val LightColorScheme = lightColorScheme(
    primary = IslamicGreen,
    onPrimary = Color.White,
    primaryContainer = IslamicGreenContainer,
    onPrimaryContainer = OnIslamicGreenContainer,
    secondary = QuranGoldDark,
    onSecondary = Color.White,
    secondaryContainer = QuranGoldContainer,
    onSecondaryContainer = OnQuranGoldContainer,
    background = ParchmentPaper,
    onBackground = ParchmentText,
    surface = ParchmentSurface,
    onSurface = ParchmentText,
    surfaceVariant = Color(0xFFEDE4D0),
    onSurfaceVariant = ParchmentTextSecondary,
    outline = ParchmentBorder
)

@Composable
fun QuranAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QuranTypography,
        content = content
    )
}
