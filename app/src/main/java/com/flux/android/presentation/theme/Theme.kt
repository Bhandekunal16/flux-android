package com.flux.android.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.Typography
import com.flux.android.data.local.AccentTheme
import com.flux.android.data.local.ThemeMode

data class FluxCustomColors(
    val accentGradient: Brush,
    val surfaceElevated: Color,
    val surfaceHigh: Color,
    val border: Color,
    val textMuted: Color,
    val isDark: Boolean,
    val onPrimaryText: Color = EditorialOnPrimary,
    val cardBackground: Color = DarkSurface,
    val outlineVariant: Color = DarkOutlineVariant,
)

val LocalFluxColors =
    staticCompositionLocalOf {
        FluxCustomColors(
            accentGradient = EditorialGradient,
            surfaceElevated = DarkSurfaceElevated,
            surfaceHigh = DarkSurfaceHigh,
            border = DarkBorder,
            textMuted = DarkTextMuted,
            isDark = true,
            onPrimaryText = EditorialOnPrimary,
            cardBackground = DarkSurface,
            outlineVariant = DarkOutlineVariant,
        )
    }

fun buildFluxColorScheme(
    accent: AccentTheme,
    isDark: Boolean,
): ColorScheme {
    val (primary, secondary, tertiary) =
        when (accent) {
            AccentTheme.EDITORIAL -> Triple(EditorialPrimary, EditorialSecondary, EditorialTertiary)
            AccentTheme.SUNSET -> Triple(SunsetPrimary, SunsetSecondary, SunsetTertiary)
            AccentTheme.OCEAN -> Triple(OceanPrimary, OceanSecondary, OceanTertiary)
            AccentTheme.FOREST -> Triple(ForestPrimary, ForestSecondary, ForestTertiary)
            AccentTheme.BERRY -> Triple(BerryPrimary, BerrySecondary, BerryTertiary)
        }

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = if (accent == AccentTheme.EDITORIAL) EditorialOnPrimary else Color.White,
            primaryContainer = if (accent == AccentTheme.EDITORIAL) EditorialPrimaryContainer else primary.copy(alpha = 0.25f),
            onPrimaryContainer = if (accent == AccentTheme.EDITORIAL) EditorialOnPrimaryContainer else primary,
            secondary = secondary,
            onSecondary = if (accent == AccentTheme.EDITORIAL) EditorialOnSecondary else Color.White,
            secondaryContainer = if (accent == AccentTheme.EDITORIAL) EditorialSecondaryContainer else secondary.copy(alpha = 0.25f),
            onSecondaryContainer = if (accent == AccentTheme.EDITORIAL) EditorialOnSecondaryContainer else secondary,
            tertiary = tertiary,
            onTertiary = if (accent == AccentTheme.EDITORIAL) EditorialOnTertiary else Color.Black,
            background = DarkCanvas,
            onBackground = DarkTextPrimary,
            surface = DarkSurface,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurfaceHigh,
            onSurfaceVariant = DarkTextSecondary,
            outline = DarkBorder,
            outlineVariant = DarkOutlineVariant,
        )
    } else {
        lightColorScheme(
            primary = if (accent == AccentTheme.EDITORIAL) Color(0xFF6750A4) else primary,
            onPrimary = Color.White,
            primaryContainer = if (accent == AccentTheme.EDITORIAL) Color(0xFFEADDFF) else primary.copy(alpha = 0.15f),
            onPrimaryContainer = if (accent == AccentTheme.EDITORIAL) Color(0xFF21005D) else primary,
            secondary = if (accent == AccentTheme.EDITORIAL) Color(0xFF625B71) else secondary,
            onSecondary = Color.White,
            secondaryContainer = if (accent == AccentTheme.EDITORIAL) Color(0xFFE8DEF8) else secondary.copy(alpha = 0.15f),
            onSecondaryContainer = if (accent == AccentTheme.EDITORIAL) Color(0xFF1D192B) else secondary,
            tertiary = if (accent == AccentTheme.EDITORIAL) Color(0xFF7D5260) else tertiary,
            onTertiary = Color.White,
            background = LightCanvas,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSurfaceElevated,
            onSurfaceVariant = LightTextSecondary,
            outline = LightBorder,
            outlineVariant = LightOutlineVariant,
        )
    }
}

@Composable
fun FluxTheme(
    mode: ThemeMode = ThemeMode.DARK,
    accent: AccentTheme = AccentTheme.EDITORIAL,
    content: @Composable () -> Unit,
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark =
        when (mode) {
            ThemeMode.SYSTEM -> systemInDark
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
        }

    val gradient =
        when (accent) {
            AccentTheme.EDITORIAL -> EditorialGradient
            AccentTheme.SUNSET -> SunsetGradient
            AccentTheme.OCEAN -> OceanGradient
            AccentTheme.FOREST -> ForestGradient
            AccentTheme.BERRY -> BerryGradient
        }

    val customColors =
        FluxCustomColors(
            accentGradient = gradient,
            surfaceElevated = if (isDark) DarkSurfaceElevated else LightSurfaceElevated,
            surfaceHigh = if (isDark) DarkSurfaceHigh else LightSurfaceHigh,
            border = if (isDark) DarkBorder else LightBorder,
            textMuted = if (isDark) DarkTextMuted else LightTextMuted,
            isDark = isDark,
            onPrimaryText = if (accent == AccentTheme.EDITORIAL) EditorialOnPrimary else Color.White,
            cardBackground = if (isDark) DarkSurface else LightSurface,
            outlineVariant = if (isDark) DarkOutlineVariant else LightOutlineVariant,
        )

    val colorScheme = buildFluxColorScheme(accent, isDark)

    CompositionLocalProvider(LocalFluxColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
