package com.example.getsafenowclient.theme_preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun TypographyPreview() = GsnTheme {
    Surface {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            with(GsnTheme.materialTypography) {
                TypographyPreviewGsn(displayLarge, "Display large")
                TypographyPreviewGsn(displayMedium, "Display medium")
                TypographyPreviewGsn(displaySmall, "Display small")
                TypographyPreviewGsn(headlineLarge, "Headline large")
                TypographyPreviewGsn(headlineMedium, "Headline medium")
                TypographyPreviewGsn(headlineSmall, "Headline small")
                TypographyPreviewGsn(titleLarge, "Title large")
                TypographyPreviewGsn(titleMedium, "Title medium")
                TypographyPreviewGsn(titleSmall, "Title small")
                TypographyPreviewGsn(bodyLarge, "Body large")
                TypographyPreviewGsn(bodyMedium, "Body medium")
                TypographyPreviewGsn(bodySmall, "Body small")
                TypographyPreviewGsn(labelLarge, "Label large")
                TypographyPreviewGsn(labelMedium, "Label medium")
                TypographyPreviewGsn(labelSmall, "Label small")
            }
        }
    }
}

@Composable
private fun TypographyPreviewGsn(style: TextStyle, text: String) {
    Text(text = text, style = style)
}