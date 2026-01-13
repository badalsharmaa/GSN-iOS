package io.getsafenow.libraries.gsn_theme.customtheme.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import io.getsafenow.libraries.gsn_theme.customtheme.theme_res.DarkColor
import io.getsafenow.libraries.gsn_theme.customtheme.themeutils.GsnCoreColorToken

@OptIn(GsnCoreColorToken::class)
fun GsnColours.toMaterialColorSchemeDark(): ColorScheme = darkColorScheme(
    primary = bgActionPrimaryRest,
    onPrimary = textOnSolidPrimary,
    primaryContainer = bgCanvasDefault,
    onPrimaryContainer = textPrimary,
    inversePrimary = textOnSolidPrimary,
    secondary = textSecondary,
    onSecondary = textOnSolidPrimary,
    secondaryContainer = bgSubtlePrimary,
    onSecondaryContainer = textPrimary,
    tertiary = textSecondary,
    onTertiary = textOnSolidPrimary,
    tertiaryContainer = bgActionPrimaryRest,
    onTertiaryContainer = textOnSolidPrimary,
    background = bgCanvasDefault,
    onBackground = textPrimary,
    surface = bgCanvasDefault,
    onSurface = textPrimary,
    surfaceVariant = bgSubtleSecondary,
    onSurfaceVariant = textSecondary,
    surfaceTint = DarkColor.colorGray1000,
    inverseSurface = DarkColor.colorGray1300,
    inverseOnSurface = textOnSolidPrimary,
    error = textCriticalPrimary,
    onError = textOnSolidPrimary,
    errorContainer = DarkColor.colorRed400,
    onErrorContainer = textCriticalPrimary,
    outline = borderInteractivePrimary,
    outlineVariant = DarkColor.colorAlphaGray400,
    // Note: for light it will be colorGray1400
    scrim = DarkColor.colorGray300,
)