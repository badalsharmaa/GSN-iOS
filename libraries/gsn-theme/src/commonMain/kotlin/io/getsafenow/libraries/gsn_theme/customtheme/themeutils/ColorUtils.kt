package io.getsafenow.libraries.gsn_theme.customtheme.themeutils

import androidx.compose.ui.graphics.Color

/**
 * Convert color to Human Readable Format.
 */
fun Color.toHrf(): String {
    return "0x" + value.toString(16).take(8).uppercase()
}
