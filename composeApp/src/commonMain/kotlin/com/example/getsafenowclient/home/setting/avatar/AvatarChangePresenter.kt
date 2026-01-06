package com.example.getsafenowclient.home.setting.avatar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.media
import net.folivo.trixnity.utils.toByteArrayFlow

@Composable
fun avatarChangePresenter(
    client: MatrixClient
): Pair<AvatarChangeState, (AvatarChangeEvent) -> Unit> {
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf(AvatarChangeState()) }

    val eventSink: (AvatarChangeEvent) -> Unit = remember {
        { event ->
            when (event) {
                is AvatarChangeEvent.AvatarSelected -> {
                    state = state.copy(isUploading = true, error = null)
                    scope.launch {
                        try {
                            // Convert ByteArray to Flow<ByteArray> for prepareUploadMedia
                            // Trixnity expects a ByteArrayFlow which is usually Flow<ByteArray>
                            val content = flowOf(event.bytes)

                            // 1. Prepare upload (caches locally)
                            val cacheUri = client.media.prepareUploadMedia(
                                content = content,
                                contentType = ContentType.Image.JPEG
                            )

                            // 2. Upload media to server
                            // Note: progress parameter is optional, keepMediaInCache defaults to true if not specified
                            // uploadMedia returns Result<String> which is the MXC URI
                            val mediaUri = client.media.uploadMedia(
                                cacheUri = cacheUri,
                                progress = null,
                                keepMediaInCache = true
                            ).getOrThrow()

                            // 3. Update user avatar
                            // Use the extension function directly on client as requested
                            client.setAvatarUrl(mediaUri).getOrThrow()

                            state = state.copy(isUploading = false)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            state = state.copy(isUploading = false, error = e.message ?: "Failed to upload avatar")
                        }
                    }
                }
                AvatarChangeEvent.ClearAvatar -> {
                    state = state.copy(isUploading = true, error = null)
                    scope.launch {
                        try {
                            client.setAvatarUrl(null).getOrThrow()
                            state = state.copy(isUploading = false)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            state = state.copy(isUploading = false, error = e.message ?: "Failed to clear avatar")
                        }
                    }
                }
                AvatarChangeEvent.DismissError -> {
                    state = state.copy(error = null)
                }
            }
        }
    }

    return state to eventSink
}
