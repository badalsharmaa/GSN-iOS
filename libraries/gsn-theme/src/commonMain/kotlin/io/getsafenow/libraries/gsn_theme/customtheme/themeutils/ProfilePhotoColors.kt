package io.getsafenow.libraries.gsn_theme.customtheme.themeutils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme

/**
 * Data class to hold profile photo colors.
 */
@Immutable
data class ProfilePhotoColors(
    /** Background color for the profile pic. */
    val background: Color,
    /** Foreground color for the profile pic. */
    val foreground: Color,
)


/**
 * Avatar colors using semantic tokens.
 */
@Composable
fun profilePhotoColors(): List<ProfilePhotoColors> {
    return listOf(
        ProfilePhotoColors(background = GsnTheme.colors.bgDecorative1, foreground = GsnTheme.colors.textDecorative1),
        ProfilePhotoColors(background = GsnTheme.colors.bgDecorative2, foreground = GsnTheme.colors.textDecorative2),
        ProfilePhotoColors(background = GsnTheme.colors.bgDecorative3, foreground = GsnTheme.colors.textDecorative3),
        ProfilePhotoColors(background = GsnTheme.colors.bgDecorative4, foreground = GsnTheme.colors.textDecorative4),
        ProfilePhotoColors(background = GsnTheme.colors.bgDecorative5, foreground = GsnTheme.colors.textDecorative5),
        ProfilePhotoColors(background = GsnTheme.colors.bgDecorative6, foreground = GsnTheme.colors.textDecorative6),
    )
}

@Composable
fun ProfilePhotoColorRow(colors: List<ProfilePhotoColors>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.forEach { color ->
            Box(
                modifier = Modifier.size(48.dp)
                    .background(color.background),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "A",
                    color = color.foreground,
                )
            }
        }
    }
}