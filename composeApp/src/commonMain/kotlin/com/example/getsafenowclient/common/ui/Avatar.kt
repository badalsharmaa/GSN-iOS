package com.example.getsafenowclient.common.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import com.example.getsafenowclient.service.asImageBitmap
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.User
import io.getsafenow.libraries.gsn_theme.customtheme.themeutils.ProfilePhotoColors
import io.getsafenow.libraries.gsn_theme.customtheme.themeutils.profilePhotoColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.media
import kotlin.math.abs

// ---------------------------------------------------------
// Main Avatar Composable
// ---------------------------------------------------------
@Composable
fun GsnAvatarAdvanced(
    modifier: Modifier = Modifier,
    id: String,
    name: String,
    url: String? = null,
    client: MatrixClient? = null,
    shape: Shape = CircleShape,
    textSize: TextUnit = 18.sp
) {
    val colors = rememberProfileColorsFor(id)
    val initials = remember(name) { computeInitials(name) }

    Box(
        modifier = modifier
            .background(colors.background, shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        when {
            // ✅ URL provided
            !url.isNullOrBlank() -> {
                if (url.startsWith("mxc://") && client != null) {
                    // Matrix-hosted image
                    GsnMatrixImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shape),
                        client = client,
                        mxcUri = url
                    )
                } else {
                    // Regular HTTP(S) image
                    AsyncImage(
                        model = url,
                        contentDescription = "avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // ✅ No URL, no name — fallback icon
            name.isBlank() -> {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.User,
                    contentDescription = "avatar",
                    tint = colors.foreground,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            // ✅ No URL but has name — show initials
            else -> {
                Text(
                    text = initials,
                    fontSize = textSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = colors.foreground
                )
            }
        }
    }
}

// ---------------------------------------------------------
// Now public (fixes "Unresolved reference rememberProfileColorsFor")
// ---------------------------------------------------------
@Composable
fun rememberProfileColorsFor(id: String): ProfilePhotoColors {
    val palette = profilePhotoColors()
    val index = abs(id.hashCode()) % palette.size
    return palette[index]
}

// ---------------------------------------------------------
// Helpers
// ---------------------------------------------------------
private fun computeInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val take = parts.take(2).map { it.first().uppercaseChar() }
    return when (take.size) {
        0 -> ""
        1 -> take[0].toString()
        else -> "${take[0]}${take[1]}"
    }
}

// ---------------------------------------------------------
// Matrix mxc:// image loader
// ---------------------------------------------------------
@Composable
fun GsnMatrixImage(
    modifier: Modifier = Modifier,
    client: MatrixClient,
    mxcUri: String
) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    // FIX: Offload decoding to background thread to prevent UI Jank
    LaunchedEffect(mxcUri, size) {
        if (size != IntSize.Zero) {
            bitmap = withContext(Dispatchers.Default) {
                try {
                    val bytes = client.media
                        .getThumbnail(mxcUri, size.width.toLong(), size.height.toLong())
                        .getOrNull()
                        ?.toByteArray()
                    bytes?.asImageBitmap()
                } catch (e: Exception) {
                    Logger.e("Error with image bytes: ($e)-> {$mxcUri}")
                    null
                }
            }
        }
    }
    Box(
        modifier = modifier.onGloballyPositioned { size = it.size }
    ) {
        bitmap?.let {
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}
