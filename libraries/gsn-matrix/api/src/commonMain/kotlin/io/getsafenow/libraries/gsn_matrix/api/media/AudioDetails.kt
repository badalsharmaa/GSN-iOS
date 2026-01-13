package io.getsafenow.libraries.gsn_matrix.api.media

import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

data class AudioDetails(
    val duration: Duration,
    val waveform: ImmutableList<Float>,
)
