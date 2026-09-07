package com.flux.android.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Accent Palettes for Flux

// 0. Editorial Aesthetic (Primary Theme: Soft Lilac, Deep Violet, Warm Mauve)
val EditorialPrimary = Color(0xFFD0BCFF)
val EditorialSecondary = Color(0xFFCCC2DC)
val EditorialTertiary = Color(0xFFEFB8C8)
val EditorialOnPrimary = Color(0xFF381E72)
val EditorialOnSecondary = Color(0xFF332D41)
val EditorialOnTertiary = Color(0xFF492532)
val EditorialPrimaryContainer = Color(0xFF4F378B)
val EditorialOnPrimaryContainer = Color(0xFFEADDFF)
val EditorialSecondaryContainer = Color(0xFF4A4458)
val EditorialOnSecondaryContainer = Color(0xFFE8DEF8)
val EditorialGradient = Brush.linearGradient(
    listOf(Color(0xFFD0BCFF), Color(0xFFCCC2DC), Color(0xFF9A82DB))
)

// 1. Sunset
val SunsetPrimary = Color(0xFFFF5722)
val SunsetSecondary = Color(0xFFFF2A6D)
val SunsetTertiary = Color(0xFFFFA000)
val SunsetGradient = Brush.linearGradient(listOf(Color(0xFFFF5722), Color(0xFFFF2A6D), Color(0xFF9D4EDD)))

// 2. Ocean
val OceanPrimary = Color(0xFF00C9FF)
val OceanSecondary = Color(0xFF0072FF)
val OceanTertiary = Color(0xFF4FACFE)
val OceanGradient = Brush.linearGradient(listOf(Color(0xFF00C9FF), Color(0xFF0072FF), Color(0xFF7000FF)))

// 3. Forest
val ForestPrimary = Color(0xFF10B981)
val ForestSecondary = Color(0xFF059669)
val ForestTertiary = Color(0xFF34D399)
val ForestGradient = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF06B6D4)))

// 4. Berry
val BerryPrimary = Color(0xFFEC4899)
val BerrySecondary = Color(0xFF8B5CF6)
val BerryTertiary = Color(0xFFF43F5E)
val BerryGradient = Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFFD946EF)))

// Editorial Dark Surfaces & Typography Canvas
val DarkCanvas = Color(0xFF1C1B1F)           // Charcoal Obsidian canvas
val DarkSurface = Color(0xFF2B2930)          // Editorial Surface
val DarkSurfaceElevated = Color(0xFF36343B)  // Card / Higher Elevation
val DarkSurfaceHigh = Color(0xFF49454F)      // Inputs / Chips / Button containers
val DarkBorder = Color(0xFF49454F)           // Editorial Border
val DarkOutlineVariant = Color(0xFF938F99)   // Subtle Secondary Outline
val DarkTextPrimary = Color(0xFFE6E1E5)      // Editorial Ivory text
val DarkTextSecondary = Color(0xFFCAC4D0)    // Muted Editorial Silver text
val DarkTextMuted = Color(0xFF938F99)        // Dimmed caption text

// Editorial Clean Crisp Light Surfaces
val LightCanvas = Color(0xFFFEF7FF)
val LightSurface = Color(0xFFF7F2FA)
val LightSurfaceElevated = Color(0xFFECE6F0)
val LightSurfaceHigh = Color(0xFFE6E0E9)
val LightBorder = Color(0xFFCAC4D0)
val LightOutlineVariant = Color(0xFF79747E)
val LightTextPrimary = Color(0xFF1D1B20)
val LightTextSecondary = Color(0xFF49454F)
val LightTextMuted = Color(0xFF79747E)
