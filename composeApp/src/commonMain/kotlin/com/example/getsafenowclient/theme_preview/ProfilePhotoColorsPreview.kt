package com.example.getsafenowclient.theme_preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import io.getsafenow.libraries.gsn_theme.customtheme.themeutils.ProfilePhotoColorRow
import io.getsafenow.libraries.gsn_theme.customtheme.themeutils.profilePhotoColors
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.collections.chunked

@Preview
@Composable
fun ProfilePhotoColorsPreviewLight() {
    GsnTheme {
        val chunks = profilePhotoColors() .chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (chunk in chunks) {
                ProfilePhotoColorRow(chunk)
            }
        }
    }
}

@Preview
@Composable
fun ProfilePhotoColorsPreviewDark() {
    GsnTheme(darkTheme = true) {
        val chunks = profilePhotoColors().chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (chunk in chunks) {
                ProfilePhotoColorRow(chunk)
            }
        }
    }
}