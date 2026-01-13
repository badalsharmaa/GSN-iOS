package com.example.getsafenowclient.theme_preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import io.getsafenow.libraries.gsn_theme.customtheme.colors.gsnColorsHcDark
import io.getsafenow.libraries.gsn_theme.customtheme.colors.gsnColorsHcLight
import io.getsafenow.libraries.gsn_theme.customtheme.themepreview.ColorsPreviewGsn
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun ColorsSchemeLightPreview() = GsnTheme {
    ColorsPreviewGsn(
        Color.Black,
        Color.White,
        GsnTheme.materialColors,
    )
}

@Preview
@Composable
fun ColorsSchemeLightHcPreview() = GsnTheme(
    gsnLight = gsnColorsHcLight,
) {
    ColorsPreviewGsn(
        Color.Black,
        Color.White,
        GsnTheme.materialColors,
    )
}

@Preview
@Composable
fun ColorsSchemeDarkPreview() = GsnTheme(
    darkTheme = true,
) {
    ColorsPreviewGsn(
        Color.White,
        Color.Black,
        GsnTheme.materialColors,
    )
}

@Preview
@Composable
fun ColorsSchemeDarkHcPreview() = GsnTheme(
    darkTheme = true,
    gsnDark = gsnColorsHcDark,
) {
    ColorsPreviewGsn(
        Color.White,
        Color.Black,
        GsnTheme.materialColors,
    )
}