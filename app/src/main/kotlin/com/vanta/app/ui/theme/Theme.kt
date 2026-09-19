package com.vanta.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * VANTA is deliberately a dark-first, single coherent visual system rather
 * than something that reshuffles itself under Material "dynamic color" —
 * see spec section 10. There is no light theme variant by design.
 */
private val VantaColorScheme = darkColorScheme(
    primary = Ion,
    onPrimary = VoidBase,
    secondary = Signal,
    onSecondary = VoidBase,
    tertiary = Ember,
    background = VoidBase,
    onBackground = TextPrimary,
    surface = VoidSurface,
    onSurface = TextPrimary,
    surfaceVariant = VoidSurfaceRaised,
    onSurfaceVariant = TextSecondary,
    outline = DividerLine,
    error = ErrorRed,
    onError = VoidBase
)

@Composable
fun VantaTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = VoidBase.toArgb()
            window.navigationBarColor = VoidBase.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = VantaColorScheme,
        typography = VantaTypography,
        content = content
    )
}
