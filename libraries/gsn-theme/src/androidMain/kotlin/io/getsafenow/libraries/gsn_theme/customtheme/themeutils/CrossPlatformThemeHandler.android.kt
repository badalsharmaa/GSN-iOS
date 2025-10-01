package io.getsafenow.libraries.gsn_theme.customtheme.themeutils

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun platformDynamicColorSchemeOrNull(darkTheme: Boolean): ColorScheme? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    return if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
}

@Composable
actual fun platformStatusBarColorScheme(
    lightStatusBar: Boolean,
    darkTheme: Boolean,
    fallback: ColorScheme
): ColorScheme {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return fallback
    val context = LocalContext.current
    // For readability, invert like your original: lightStatusBar uses dark scheme and vice versa.
    return if (lightStatusBar) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }
}

@SuppressLint("ContextCastToActivity", "ComposableNaming")
@Composable
actual fun applyPlatformSystemBarsUpdate(
    statusBarScheme: ColorScheme,
    darkTheme: Boolean,
    lightStatusBar: Boolean
) {
    val activity = LocalContext.current as? ComponentActivity
    LaunchedEffect(statusBarScheme, darkTheme, lightStatusBar) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = statusBarScheme.background.toArgb(),
                darkScrim  = statusBarScheme.background.toArgb(),
                detectDarkMode = { !lightStatusBar }
            ),
            navigationBarStyle = if (darkTheme) {
                SystemBarStyle.dark(Color.Transparent.toArgb())
            } else {
                SystemBarStyle.light(
                    Color.Transparent.toArgb(),
                    Color.Transparent.toArgb()
                )
            }
        )
    }
}


@SuppressLint("ContextCastToActivity")
@Composable
actual fun PlatformRestoreSystemBarsOnDispose(
    currentScheme: ColorScheme,
    wasDarkTheme: Boolean,
) {
    val activity = LocalContext.current as? ComponentActivity
    // Mirror your original behavior: when the forced-dark scope ends, put bars back.
    DisposableEffect(currentScheme, wasDarkTheme) {
        onDispose {
            activity?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    lightScrim = currentScheme.background.toArgb(),
                    darkScrim  = currentScheme.background.toArgb(),
                ),
                navigationBarStyle = if (wasDarkTheme) {
                    SystemBarStyle.dark(Color.Transparent.toArgb())
                } else {
                    SystemBarStyle.light(
                        scrim     = Color.Transparent.toArgb(),
                        darkScrim = Color.Transparent.toArgb()
                    )
                }
            )
        }
    }
}