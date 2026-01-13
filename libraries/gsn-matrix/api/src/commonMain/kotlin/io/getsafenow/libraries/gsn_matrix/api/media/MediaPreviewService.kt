package io.getsafenow.libraries.gsn_matrix.api.media

import kotlinx.coroutines.flow.StateFlow

interface MediaPreviewService {
    /**
     * Will fetch the media preview config from the server.
     */
    suspend fun fetchMediaPreviewConfig(): Result<MediaPreviewConfig?>

    /**
     * Will emit the media preview config known by the client.
     * This will emit a new value when received from sync.
     */
    val mediaPreviewConfigFlow: StateFlow<MediaPreviewConfig>

    /**
     * Set the media preview display policy. This will update the value on the server and update the local value when successful.
     */
    suspend fun setMediaPreviewValue(mediaPreviewValue: MediaPreviewValue): Result<Unit>

    /**
     * Set the invite avatars display policy. This will update the value on the server and update the local value when successful.
     */
    suspend fun setHideInviteAvatars(hide: Boolean): Result<Unit>
}

fun MediaPreviewService.getMediaPreviewValue() = mediaPreviewConfigFlow.value.mediaPreviewValue
