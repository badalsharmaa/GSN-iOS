package io.getsafenow.libraries.gsn_theme.customtheme.themeutils

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme

/**
 * Enforces dark theme while this composable is in the composition.
 * When it leaves composition, platform impl can restore system bars.
 */
@Composable
fun EnforcedDarkTheme(
    lightStatusBar: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val wasDarkTheme = !GsnTheme.isLightTheme

    // Let each platform hook system bars (Android does; iOS/JVM no-op).
    PlatformRestoreSystemBarsOnDispose(
        currentScheme = colorScheme,
        wasDarkTheme = wasDarkTheme
    )

    GsnTheme(
        darkTheme = true,
        lightStatusBar = lightStatusBar,
        content = content
    )
}

/** Platform hook used to restore system bars when this leaves composition. */
@Composable
expect fun PlatformRestoreSystemBarsOnDispose(
    currentScheme: ColorScheme,
    wasDarkTheme: Boolean,
)