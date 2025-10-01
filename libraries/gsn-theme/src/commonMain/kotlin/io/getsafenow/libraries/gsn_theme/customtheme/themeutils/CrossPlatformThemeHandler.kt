package io.getsafenow.libraries.gsn_theme.customtheme.themeutils

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * If the platform supports Material You (Android 12+), return a dynamic ColorScheme.
 * Otherwise return null. iOS returns null.
 */
@Composable
expect fun platformDynamicColorSchemeOrNull(darkTheme: Boolean): ColorScheme?

/**
 * Provide the ColorScheme to use for status/navigation bars.
 * Android may return a dynamic scheme; iOS returns [fallback].
 */
@Composable
expect fun platformStatusBarColorScheme(
    lightStatusBar: Boolean,
    darkTheme: Boolean,
    fallback: ColorScheme
): ColorScheme

/**
 * Apply system bar styling (status/navigation). Android updates bars; iOS is no-op.
 */
@Composable
expect fun applyPlatformSystemBarsUpdate(
    statusBarScheme: ColorScheme,
    darkTheme: Boolean,
    lightStatusBar: Boolean
)
