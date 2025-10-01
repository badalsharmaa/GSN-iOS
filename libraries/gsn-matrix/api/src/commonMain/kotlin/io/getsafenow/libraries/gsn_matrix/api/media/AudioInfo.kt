package io.getsafenow.libraries.gsn_matrix.api.media

import kotlin.time.Duration

data class AudioInfo(
    val duration: Duration?,
    val size: Long?,
    val mimetype: String?,
)
