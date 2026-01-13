package io.getsafenow.libraries.gsn_matrix.api.media

import io.getsafenow.libraries.gsn_matrix.api.media.MediaPreviewValue.Off
import io.getsafenow.libraries.gsn_matrix.api.media.MediaPreviewValue.On
import io.getsafenow.libraries.gsn_matrix.api.media.MediaPreviewValue.Private
import io.getsafenow.libraries.gsn_matrix.api.room.join.JoinRule

/**
 * Represents the values for media preview settings.
 * - [On] means that media preview are enabled
 * - [Off] means that media preview are disabled
 * - [Private] means that media preview are enabled only for private chats.
 */
enum class MediaPreviewValue {
    On,
    Off,
    Private;

    companion object {
        /**
         * The default value if unknown (no local nor server config).
         */
        val DEFAULT = On
    }
}

fun MediaPreviewValue?.isPreviewEnabled(joinRule: JoinRule?): Boolean {
    return when (this) {
        null, On -> true
        Off -> false
        Private -> when (joinRule) {
            is JoinRule.Private,
            is JoinRule.Knock,
            is JoinRule.Invite,
            is JoinRule.Restricted,
            is JoinRule.KnockRestricted -> true
            else -> false
        }
    }
}
