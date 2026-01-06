package io.getsafenow.libraries.preferences.api

/**
 * Video compression presets to use when processing videos before uploading them.
 */
enum class VideoCompressionPreset {
    /** High quality compression, suitable for high-resolution videos. */
    HIGH,

    /** Standard quality compression, suitable for most videos. */
    STANDARD,

    /** Low quality compression, suitable for low-resolution videos or when bandwidth is a concern. */
    LOW
}
