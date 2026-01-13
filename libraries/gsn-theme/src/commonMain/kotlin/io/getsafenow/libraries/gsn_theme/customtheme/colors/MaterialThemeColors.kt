package io.getsafenow.libraries.gsn_theme.customtheme.colors

import androidx.compose.material3.ColorScheme


fun GsnColours.toMaterialColorSchemeGsn(): ColorScheme {
    return if (isLight) {
        toMaterialColorSchemeLight()
    } else {
        toMaterialColorSchemeDark()
    }
}


