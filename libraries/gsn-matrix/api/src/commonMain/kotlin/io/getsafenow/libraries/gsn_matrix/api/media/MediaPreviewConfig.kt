package io.getsafenow.libraries.gsn_matrix.api.media

/**
 * Configuration for media preview ie. invite avatars and timeline media.
 */
data class MediaPreviewConfig(
    val mediaPreviewValue: MediaPreviewValue,
    val hideInviteAvatar: Boolean,
) {
    companion object {
        /**
         * The default config if unknown (no local nor server config).
         */
        val DEFAULT = MediaPreviewConfig(
            mediaPreviewValue = MediaPreviewValue.Companion.DEFAULT,
            hideInviteAvatar = false
        )
    }
}
