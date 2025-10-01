package io.getsafenow.libraries.gsn_theme.customtheme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import io.getsafenow.libraries.gsn_theme.customtheme.colors.GsnColours
import io.getsafenow.libraries.gsn_theme.customtheme.colors.gsnColorsDark
import io.getsafenow.libraries.gsn_theme.customtheme.colors.gsnColorsLight
import io.getsafenow.libraries.gsn_theme.customtheme.colors.toMaterialColorSchemeGsn
import io.getsafenow.libraries.gsn_theme.customtheme.theme_res.TypographyGsn
import io.getsafenow.libraries.gsn_theme.customtheme.themeutils.applyPlatformSystemBarsUpdate
import io.getsafenow.libraries.gsn_theme.customtheme.themeutils.platformDynamicColorSchemeOrNull
import io.getsafenow.libraries.gsn_theme.customtheme.themeutils.platformStatusBarColorScheme
import io.getsafenow.libraries.gsn_theme.customtheme.typography.materialTypographyGsn


/**
 * Inspired from https://medium.com/@lucasyujideveloper/54cbcbde1ace
 */
object GsnTheme {
    val colors: GsnColours
        @Composable get() = LocalCompoundColors.current
    val materialColors: ColorScheme
        @Composable get() = MaterialTheme.colorScheme
    val typography: TypographyGsn = TypographyGsn
    val materialTypography: androidx.compose.material3.Typography
        @Composable get() = MaterialTheme.typography
    val isLightTheme: Boolean
        @Composable get() = LocalCompoundColors.current.isLight
}

internal val LocalCompoundColors = staticCompositionLocalOf { gsnColorsLight }

@Composable
fun GsnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    applySystemBarsUpdate: Boolean = true,
    lightStatusBar: Boolean = !darkTheme,
    dynamicColor: Boolean = false, // only Android will honor this
    gsnLight: GsnColours = gsnColorsLight,
    gsnDark: GsnColours = gsnColorsDark,
    materialColorsLight: ColorScheme = gsnLight.toMaterialColorSchemeGsn(),
    materialColorsDark: ColorScheme = gsnDark.toMaterialColorSchemeGsn(),
    typography: androidx.compose.material3.Typography = materialTypographyGsn,
    content: @Composable () -> Unit,
) {
    val semantic = if (darkTheme) gsnDark else gsnLight

    // Ask platform for dynamic scheme (Android 12+), else fall back to provided schemes.
    val maybeDynamic: ColorScheme? = remember(dynamicColor, darkTheme) {
        null // default for non-Android platforms (overridden by actual on Android)
    }
    val dynamicScheme = if (dynamicColor) platformDynamicColorSchemeOrNull(darkTheme) else null

    val colorScheme = when {
        dynamicScheme != null -> dynamicScheme
        darkTheme -> materialColorsDark
        else -> materialColorsLight
    }

    val statusBarScheme = platformStatusBarColorScheme(
        lightStatusBar = lightStatusBar,
        darkTheme = darkTheme,
        fallback = colorScheme
    )

    if (applySystemBarsUpdate) {
        applyPlatformSystemBarsUpdate(
            statusBarScheme = statusBarScheme,
            darkTheme = darkTheme,
            lightStatusBar = lightStatusBar
        )
    }

    CompositionLocalProvider(
        LocalCompoundColors provides semantic,
        LocalContentColor provides colorScheme.onSurface,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}