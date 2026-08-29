package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Orange & White Palette
val OrangePrimary = Color(0xFFFF6B00)
val OrangePrimaryDark = Color(0xFFEA580C)
val OrangePrimaryLight = Color(0xFFFFA043)
val OrangePrimaryContainer = Color(0xFFFFF4EC)
val OrangeOnPrimaryContainer = Color(0xFF7C2D12)
val OrangeSecondary = Color(0xFFFB923C)

val WarmPeachSecondary = Color(0xFFFB923C)
val WarmPeachSecondaryContainer = Color(0xFFFFEDD5)
val WarmPeachOnSecondaryContainer = Color(0xFF9A3412)

val WarmAmberTertiary = Color(0xFFF59E0B)
val WarmAmberTertiaryContainer = Color(0xFFFEF3C7)
val WarmAmberOnTertiaryContainer = Color(0xFF78350F)

// Light Clean Background & Surfaces (Crisp White + Warm Accents)
val PureWhite = Color(0xFFFFFFFF)
val WarmOffWhite = Color(0xFFFAFAF8)
val WarmSurfaceVariant = Color(0xFFF6F3EE)
val WarmBorder = Color(0xFFEADBCE)
val WarmTextDark = Color(0xFF1C1917)
val WarmTextMuted = Color(0xFF78716C)

// Backward compatible aliases mapped to the Orange & White theme
val NavyPrimary = OrangePrimary
val NavyOnPrimary = PureWhite
val NavyPrimaryContainer = OrangePrimaryContainer
val NavyOnPrimaryContainer = OrangeOnPrimaryContainer

val TealSecondary = OrangePrimaryDark
val TealOnSecondary = PureWhite
val TealSecondaryContainer = WarmPeachSecondaryContainer
val TealOnSecondaryContainer = WarmPeachOnSecondaryContainer

val AmberTertiary = WarmAmberTertiary
val AmberOnTertiary = PureWhite
val AmberTertiaryContainer = WarmAmberTertiaryContainer
val AmberOnTertiaryContainer = WarmAmberOnTertiaryContainer

val SlateBackground = WarmOffWhite
val SlateOnBackground = WarmTextDark
val SlateSurface = PureWhite
val SlateOnSurface = WarmTextDark
val SlateSurfaceVariant = WarmSurfaceVariant
val SlateOnSurfaceVariant = WarmTextMuted
val SlateOutline = WarmBorder

// Solid Black & High-Contrast text for forms and inputs
val InputTextBlack = Color(0xFF111827)
val InputLabelDark = Color(0xFF374151)
val InputPlaceholderMuted = Color(0xFF9CA3AF)
val InputBorderDefault = Color(0xFFD1D5DB)

// Status colors aligned with warm theme
val SuccessGreen = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)
val InfoSky = Color(0xFF38BDF8)
val PurpleAccent = Color(0xFFA855F7)

// Beautiful Gradients
val VibrantOrangeGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF5400), Color(0xFFFF7A00), Color(0xFFFF9E3D))
)
val SunsetOrangeGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFEA580C), Color(0xFFF97316), Color(0xFFFB923C))
)
val CardWarmGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFBF7), Color(0xFFFFFFFF))
)
val HeroBannerGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF5400), Color(0xFFFF7A1A), Color(0xFFFF9A3C))
)

// Dark Palette (Warm Charcoal + Glowing Orange)
val DarkNavyPrimary = Color(0xFFFF881A)
val DarkNavyOnPrimary = Color(0xFF1C1917)
val DarkNavyPrimaryContainer = Color(0xFF7C2D12)
val DarkNavyOnPrimaryContainer = Color(0xFFFFEDD5)

val DarkTealSecondary = Color(0xFFFDBA74)
val DarkTealOnSecondary = Color(0xFF431407)
val DarkTealSecondaryContainer = Color(0xFF9A3412)
val DarkTealOnSecondaryContainer = Color(0xFFFFEDD5)

val DarkAmberTertiary = Color(0xFFFBBF24)
val DarkAmberOnTertiary = Color(0xFF451A03)
val DarkAmberTertiaryContainer = Color(0xFF92400E)
val DarkAmberOnTertiaryContainer = Color(0xFFFEF3C7)

val DarkSlateBackground = Color(0xFF141210)
val DarkSlateOnBackground = Color(0xFFFAFAF9)
val DarkSlateSurface = Color(0xFF1F1D1A)
val DarkSlateOnSurface = Color(0xFFFAFAF9)
val DarkSlateSurfaceVariant = Color(0xFF2B2824)
val DarkSlateOnSurfaceVariant = Color(0xFFA8A29E)
val DarkSlateOutline = Color(0xFF44403C)


