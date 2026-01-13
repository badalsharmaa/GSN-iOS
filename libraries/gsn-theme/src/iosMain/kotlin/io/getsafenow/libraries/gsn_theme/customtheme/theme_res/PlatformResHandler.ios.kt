package io.getsafenow.libraries.gsn_theme.customtheme.theme_res

import androidx.compose.ui.text.PlatformParagraphStyle
import androidx.compose.ui.text.PlatformSpanStyle
import androidx.compose.ui.text.PlatformTextStyle

actual fun platformTextStyleNoFontPadding(): PlatformTextStyle? =
    PlatformTextStyle(
        spanStyle = PlatformSpanStyle.Default,
        paragraphStyle = PlatformParagraphStyle.Default
    )