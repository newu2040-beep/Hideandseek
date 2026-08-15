package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun createColorSchemeForPreset(preset: VaultThemePreset): ColorScheme {
    return if (preset.isDark) {
        darkColorScheme(
            primary = preset.primary,
            onPrimary = if (preset.isDark) Color.White else Color.Black,
            primaryContainer = preset.primaryVariant,
            onPrimaryContainer = Color.White,
            secondary = preset.secondary,
            onSecondary = Color.Black,
            secondaryContainer = preset.surfaceVariant,
            onSecondaryContainer = preset.secondary,
            tertiary = preset.tertiary,
            background = preset.background,
            onBackground = preset.textPrimary,
            surface = preset.surface,
            onSurface = preset.textPrimary,
            surfaceVariant = preset.surfaceVariant,
            onSurfaceVariant = preset.textSecondary,
            outline = preset.border,
            error = AccentRed,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = preset.primary,
            onPrimary = Color.White,
            primaryContainer = preset.surfaceVariant,
            onPrimaryContainer = preset.primaryVariant,
            secondary = preset.secondary,
            onSecondary = Color.White,
            secondaryContainer = preset.surfaceVariant,
            onSecondaryContainer = preset.secondary,
            tertiary = preset.tertiary,
            background = preset.background,
            onBackground = preset.textPrimary,
            surface = preset.surface,
            onSurface = preset.textPrimary,
            surfaceVariant = preset.surfaceVariant,
            onSurfaceVariant = preset.textSecondary,
            outline = preset.border,
            error = AccentRed,
            onError = Color.White
        )
    }
}

@Composable
fun HideAndSeekTheme(
    themeMode: String = "SUPER_AMOLED_BLACK",
    content: @Composable () -> Unit
) {
    val preset = VaultThemePreset.fromId(themeMode)
    val colorScheme = createColorSchemeForPreset(preset)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = preset.background.toArgb()
                it.navigationBarColor = preset.background.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = !preset.isDark
                    isAppearanceLightNavigationBars = !preset.isDark
                }
            }
        }
    }

    CompositionLocalProvider(LocalVaultTheme provides preset) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
