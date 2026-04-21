package com.app.nyerocos.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Satu color scheme saja — Neo-Brutalist is always light
private val NyerocosColorScheme = lightColorScheme(
    primary = NyerocosBlack,
    onPrimary = NyerocosSurface,
    primaryContainer = NyerocosYellow,
    onPrimaryContainer = NyerocosBlack,
    secondary = NyerocosRed,
    onSecondary = NyerocosSurface,
    tertiary = NyerocosBlue,
    onTertiary = NyerocosSurface,
    background = NyerocosBackground,
    onBackground = NyerocosBlack,
    surface = NyerocosSurface,
    onSurface = NyerocosBlack,
    surfaceVariant = NyerocosSurfaceContainer,
    onSurfaceVariant = NyerocosOnSurfaceVariant,
    outline = NyerocosBlack,
    outlineVariant = NyerocosOutlineVariant,
)

@Composable
fun NyerocosTheme(content: @Composable () -> Unit) {
    // Status bar warna = background Nyerocos
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NyerocosBackground.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = true  // icon status bar = gelap
        }
    }

    MaterialTheme(
        colorScheme = NyerocosColorScheme,
        typography = Typography,
        content = content
    )
}
