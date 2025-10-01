package io.getsafenow.libraries.gsn_theme.customtheme.theme_res

import androidx.compose.ui.text.PlatformTextStyle

/** Android: returns PlatformTextStyle(includeFontPadding = false); iOS: returns null. */
expect fun platformTextStyleNoFontPadding(): PlatformTextStyle?