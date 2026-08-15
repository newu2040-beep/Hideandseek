package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark Palette (OLED Slate & Neon Glow)
val DarkBackground = Color(0xFF0D0F17)
val DarkSurface = Color(0xFF161824)
val DarkSurfaceVariant = Color(0xFF1E2130)
val DarkBorder = Color(0xFF2B2F44)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFF8E92A8)
val DarkTextTertiary = Color(0xFF5C6078)

// Light Palette (Soft Frosted Canvas & Deep Slate)
val LightBackground = Color(0xFFF3F4F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFECEEF6)
val LightBorder = Color(0xFFE2E4EE)
val LightTextPrimary = Color(0xFF131520)
val LightTextSecondary = Color(0xFF6B6E84)
val LightTextTertiary = Color(0xFF9EA2BA)

// Brand & Neon Accents
val AccentPurple = Color(0xFF7C4DFF)
val AccentPurpleLight = Color(0xFF9E67FF)
val AccentPurpleDark = Color(0xFF5B2EEB)
val AccentCyan = Color(0xFF00E5FF)
val AccentBlue = Color(0xFF3D5AFE)
val AccentRed = Color(0xFFFF453A)
val AccentGreen = Color(0xFF34C759)
val AccentOrange = Color(0xFFFF9F0A)

// Gradients
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF8A2BE2), Color(0xFF4A00E0))
)

val GlowGradient = Brush.sweepGradient(
    colors = listOf(
        Color(0xFF8A2BE2),
        Color(0xFF00E5FF),
        Color(0xFF9E67FF),
        Color(0xFF00E5FF),
        Color(0xFF8A2BE2)
    )
)

val CardGlowDark = Brush.verticalGradient(
    colors = listOf(Color(0xFF252940), Color(0xFF161824))
)

val CardGlowLight = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF7F8FC))
)
