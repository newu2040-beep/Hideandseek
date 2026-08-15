package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Modern Themes & Super AMOLED Display Theme Presets
 */
enum class VaultThemePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val isDark: Boolean,
    val isAmoled: Boolean,
    val category: String, // "AMOLED", "DARK", "LIGHT"
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val tertiary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentBadge: String,
    val previewGlow: Brush
) {
    SUPER_AMOLED_BLACK(
        id = "SUPER_AMOLED_BLACK",
        title = "Super AMOLED Pitch Black",
        subtitle = "True 0x000000 pixels OFF, maximum OLED battery savings with Electric Cyan & Violet accents",
        isDark = true,
        isAmoled = true,
        category = "AMOLED",
        background = Color(0xFF000000),
        surface = Color(0xFF040406),
        surfaceVariant = Color(0xFF0D0E14),
        border = Color(0xFF1F2230),
        primary = Color(0xFF9E67FF),
        primaryVariant = Color(0xFF7C4DFF),
        secondary = Color(0xFF00E5FF),
        tertiary = Color(0xFF00FF9D),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFA0A5BD),
        textTertiary = Color(0xFF62667C),
        accentBadge = "⚡ 0% AMOLED Power",
        previewGlow = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF9E67FF)))
    ),

    SUPER_AMOLED_CYBERPUNK(
        id = "SUPER_AMOLED_CYBERPUNK",
        title = "Super AMOLED Cyberpunk Neon",
        subtitle = "Pure black OLED display with vivid Neon Magenta & Electric Cyan contrast",
        isDark = true,
        isAmoled = true,
        category = "AMOLED",
        background = Color(0xFF000000),
        surface = Color(0xFF080209),
        surfaceVariant = Color(0xFF140517),
        border = Color(0xFF380E38),
        primary = Color(0xFFFF007F),
        primaryVariant = Color(0xFFD6006B),
        secondary = Color(0xFF00F0FF),
        tertiary = Color(0xFFFFD600),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFA889A5),
        textTertiary = Color(0xFF6E526B),
        accentBadge = "⚡ AMOLED Neon",
        previewGlow = Brush.horizontalGradient(listOf(Color(0xFFFF007F), Color(0xFF00F0FF)))
    ),

    SUPER_AMOLED_EMERALD(
        id = "SUPER_AMOLED_EMERALD",
        title = "Super AMOLED Matrix Emerald",
        subtitle = "Pure black OLED display with high-contrast Radiant Matrix Green & Mint",
        isDark = true,
        isAmoled = true,
        category = "AMOLED",
        background = Color(0xFF000000),
        surface = Color(0xFF020A05),
        surfaceVariant = Color(0xFF07170B),
        border = Color(0xFF0F3B1D),
        primary = Color(0xFF00FF66),
        primaryVariant = Color(0xFF00CC52),
        secondary = Color(0xFF00E676),
        tertiary = Color(0xFF69F0AE),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF86AB91),
        textTertiary = Color(0xFF4D6E57),
        accentBadge = "⚡ AMOLED Matrix",
        previewGlow = Brush.horizontalGradient(listOf(Color(0xFF00FF66), Color(0xFF00E676)))
    ),

    SUPER_AMOLED_SUNSET(
        id = "SUPER_AMOLED_SUNSET",
        title = "Super AMOLED Sunset Amber",
        subtitle = "Pitch black OLED display with radiant Amber Gold & Flame Orange highlights",
        isDark = true,
        isAmoled = true,
        category = "AMOLED",
        background = Color(0xFF000000),
        surface = Color(0xFF0A0702),
        surfaceVariant = Color(0xFF160E04),
        border = Color(0xFF3D250C),
        primary = Color(0xFFFFB300),
        primaryVariant = Color(0xFFFF8F00),
        secondary = Color(0xFFFF6D00),
        tertiary = Color(0xFFFFD54F),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFAA9A86),
        textTertiary = Color(0xFF706150),
        accentBadge = "⚡ AMOLED Gold",
        previewGlow = Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF6D00)))
    ),

    DARK_SLATE(
        id = "DARK",
        title = "Dark Slate Neon",
        subtitle = "Modern graphite slate with glowing Violet & Cyan accents",
        isDark = true,
        isAmoled = false,
        category = "DARK",
        background = Color(0xFF0D0F17),
        surface = Color(0xFF161824),
        surfaceVariant = Color(0xFF1E2130),
        border = Color(0xFF2B2F44),
        primary = Color(0xFF9E67FF),
        primaryVariant = Color(0xFF7C4DFF),
        secondary = Color(0xFF00E5FF),
        tertiary = Color(0xFF3D5AFE),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF8E92A8),
        textTertiary = Color(0xFF5C6078),
        accentBadge = "Default Slate",
        previewGlow = Brush.horizontalGradient(listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF)))
    ),

    MIDNIGHT_NAVY(
        id = "MIDNIGHT_NAVY",
        title = "Midnight Deep Navy",
        subtitle = "Oceanic indigo canvas with Arctic Blue & Ice highlights",
        isDark = true,
        isAmoled = false,
        category = "DARK",
        background = Color(0xFF0B0F19),
        surface = Color(0xFF121B2E),
        surfaceVariant = Color(0xFF18233C),
        border = Color(0xFF223456),
        primary = Color(0xFF448AFF),
        primaryVariant = Color(0xFF2979FF),
        secondary = Color(0xFF40C4FF),
        tertiary = Color(0xFF82B1FF),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF8C9EBF),
        textTertiary = Color(0xFF55688F),
        accentBadge = "Deep Navy",
        previewGlow = Brush.horizontalGradient(listOf(Color(0xFF448AFF), Color(0xFF40C4FF)))
    ),

    TITANIUM_STEALTH(
        id = "TITANIUM_STEALTH",
        title = "Titanium Stealth Monochrome",
        subtitle = "Ultra-minimalist matte carbon with pure white & metallic silver accents",
        isDark = true,
        isAmoled = false,
        category = "DARK",
        background = Color(0xFF121214),
        surface = Color(0xFF1B1B1E),
        surfaceVariant = Color(0xFF27272B),
        border = Color(0xFF38383E),
        primary = Color(0xFFE5E5EA),
        primaryVariant = Color(0xFFD1D1D6),
        secondary = Color(0xFFFFFFFF),
        tertiary = Color(0xFFAEAEB2),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFA0A0A8),
        textTertiary = Color(0xFF63636C),
        accentBadge = "Stealth",
        previewGlow = Brush.horizontalGradient(listOf(Color(0xFFE5E5EA), Color(0xFF8E8E93)))
    ),

    FROSTED_LIGHT(
        id = "FROSTED_LIGHT",
        title = "Frosted Glacier Light",
        subtitle = "Clean modern frosted light canvas with crisp white cards and royal purple",
        isDark = false,
        isAmoled = false,
        category = "LIGHT",
        background = Color(0xFFF3F4F9),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFECEEF6),
        border = Color(0xFFE0E2EC),
        primary = Color(0xFF7C4DFF),
        primaryVariant = Color(0xFF5B2EEB),
        secondary = Color(0xFF3D5AFE),
        tertiary = Color(0xFF00B4D8),
        textPrimary = Color(0xFF131520),
        textSecondary = Color(0xFF6B6E84),
        textTertiary = Color(0xFF9EA2BA),
        accentBadge = "Clean Light",
        previewGlow = Brush.horizontalGradient(listOf(Color(0xFF7C4DFF), Color(0xFF3D5AFE)))
    ),

    WARM_CREAM_LIGHT(
        id = "WARM_CREAM_LIGHT",
        title = "Warm Nordic Cream",
        subtitle = "Organic soft paper tone with terracotta & warm amber accents",
        isDark = false,
        isAmoled = false,
        category = "LIGHT",
        background = Color(0xFFF8F5EE),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFEFECE2),
        border = Color(0xFFE2DDD2),
        primary = Color(0xFFD9534F),
        primaryVariant = Color(0xFFC9302C),
        secondary = Color(0xFFE67E22),
        tertiary = Color(0xFFD35400),
        textPrimary = Color(0xFF2C2523),
        textSecondary = Color(0xFF7A6E68),
        textTertiary = Color(0xFFA89F98),
        accentBadge = "Organic",
        previewGlow = Brush.horizontalGradient(listOf(Color(0xFFD9534F), Color(0xFFE67E22)))
    );

    companion object {
        fun fromId(id: String?): VaultThemePreset {
            if (id == null) return SUPER_AMOLED_BLACK
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
                ?: if (id.equals("LIGHT", ignoreCase = true)) FROSTED_LIGHT else DARK_SLATE
        }
    }
}

val LocalVaultTheme = staticCompositionLocalOf { VaultThemePreset.SUPER_AMOLED_BLACK }

/**
 * Access the active vault theme colors conveniently
 */
object VaultTheme {
    val current: VaultThemePreset
        @Composable
        @ReadOnlyComposable
        get() = LocalVaultTheme.current
}
