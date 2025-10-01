package io.getsafenow.libraries.gsn_theme.customtheme.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import io.getsafenow.libraries.gsn_theme.customtheme.theme_res.LightColor
import io.getsafenow.libraries.gsn_theme.customtheme.themeutils.GsnCoreColorToken

@OptIn(GsnCoreColorToken::class)
fun GsnColours.toMaterialColorSchemeLight(): ColorScheme = lightColorScheme(
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
    surfaceTint = LightColor.colorGray1000,
    inverseSurface = LightColor.colorGray1300,
    inverseOnSurface = textOnSolidPrimary,
    error = textCriticalPrimary,
    onError = textOnSolidPrimary,
    errorContainer = LightColor.colorRed400,
    onErrorContainer = textCriticalPrimary,
    outline = borderInteractivePrimary,
    outlineVariant = LightColor.colorAlphaGray400,
    // Note: for dark it will be colorGray300
    scrim = LightColor.colorGray1400,
)