package io.getsafenow.libraries.designcomponents.utils.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalInspectionMode
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import io.getsafenow.sharedres.generated.resources.Res
import io.getsafenow.sharedres.generated.resources.test_background
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource


/**
 * KMP-friendly preview wrapper.
 * In preview/inspection mode, Coil uses the provided AsyncImagePreviewHandler.
 * We return a safe placeholder Image (solid 1x1) so previews never crash.
 */

@OptIn(ExperimentalCoilApi::class)
@Composable
fun GsnPreview(
    darkTheme: Boolean = isSystemInDarkTheme(),
    showBackground: Boolean = true,
    placeholderColor: Color = if (darkTheme) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error,
    content: @Composable () -> Unit
) {
    // Preview-only handler. No resource I/O, no crashes.
    val previewHandler = AsyncImagePreviewHandler { _, _ ->
        AsyncImagePainter.State.Loading(ColorPainter(placeholderColor))
    }

    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        GsnTheme{
            if (showBackground) Surface { content() } else content()
        }
    }
}


