package io.getsafenow.libraries.gsn_theme.customtheme.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.getsafenow.libraries.gsn_theme.customtheme.theme_res.platformTextStyleNoFontPadding
import io.getsafenow.libraries.gsn_theme.customtheme.theme_res.TypographyGsn

// 32px (Material) vs 34px, it's the closest one
internal val compoundHeadingXlRegular = TypographyGsn.fontHeadingXlRegular

// both are 28px
internal val compoundHeadingLgRegular = TypographyGsn.fontHeadingLgRegular

// These are the default M3 values, but we're setting them manually so an update in M3 doesn't break our designs
internal val defaultHeadlineSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    lineHeight = 32.sp,
    fontSize = 24.sp,
    letterSpacing = 0.em,
    platformStyle = platformTextStyleNoFontPadding(),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
)

// 22px (Material) vs 20px, it's the closest one

internal val compoundHeadingMdRegular = TypographyGsn.fontHeadingMdRegular

// 16px both
internal val compoundBodyLgMedium = TypographyGsn.fontBodyLgMedium

// 14px both
internal val compoundBodyMdMedium = TypographyGsn.fontBodyMdMedium

// 16px both
internal val compoundBodyLgRegular = TypographyGsn.fontBodyLgRegular

// 14px both
internal val compoundBodyMdRegular = TypographyGsn.fontBodyMdRegular

// 12px both
internal val compoundBodySmRegular = TypographyGsn.fontBodySmRegular

// 14px both, Title Small uses the same token so we have to declare it twice
internal val compoundBodyMdMedium_LabelLarge = TypographyGsn.fontBodyMdMedium

// 12px both
internal val compoundBodySmMedium = TypographyGsn.fontBodySmMedium

// 11px both
internal val compoundBodyXsMedium = TypographyGsn.fontBodyXsMedium

internal val materialTypographyGsn = Typography(
    // displayLarge = , 57px (Material) size. We have no equivalent
    // displayMedium = , 45px (Material) size. We have no equivalent
    // displaySmall = , 36px (Material) size. We have no equivalent
    headlineLarge = compoundHeadingXlRegular,
    headlineMedium = compoundHeadingLgRegular,
    headlineSmall = defaultHeadlineSmall,
    titleLarge = compoundHeadingMdRegular,
    titleMedium = compoundBodyLgMedium,
    titleSmall = compoundBodyMdMedium,
    bodyLarge = compoundBodyLgRegular,
    bodyMedium = compoundBodyMdRegular,
    bodySmall = compoundBodySmRegular,
    labelLarge = compoundBodyMdMedium_LabelLarge,
    labelMedium = compoundBodySmMedium,
    labelSmall = compoundBodyXsMedium,
)