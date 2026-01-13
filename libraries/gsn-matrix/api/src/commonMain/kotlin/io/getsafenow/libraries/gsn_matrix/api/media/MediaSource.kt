package io.getsafenow.libraries.gsn_matrix.api.media

import kotlinx.serialization.Serializable


@Serializable
data class MediaSource(
    /**
     * Url of the media.
     */
    val url: String,
    /**
     * This is used to hold data for encrypted media.
     */
    val json: String? = null,
)
