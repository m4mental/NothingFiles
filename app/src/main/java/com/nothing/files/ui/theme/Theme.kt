package com.nothing.files.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NothingRed,
    onPrimary = NothingWhite,
    primaryContainer = NothingRedMuted,
    onPrimaryContainer = NothingWhite,
    secondary = NothingWhite,
    onSecondary = NothingBlack,
    secondaryContainer = NothingSurfaceElevated,
    onSecondaryContainer = NothingWhite,
    tertiary = NothingRedBright,
    onTertiary = NothingWhite,
    background = NothingBlack,
    onBackground = NothingWhite,
    surface = NothingSurface,
    onSurface = NothingWhite,
    surfaceVariant = NothingSurfaceElevated,
    onSurfaceVariant = NothingTextSecondary,
    outline = NothingBorder,
    outlineVariant = NothingBorderLight
)

@Composable
fun NothingFilesTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = NothingBlack.toArgb()
                window.navigationBarColor = NothingBlack.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = NothingTypography,
        content = content
    )
}
