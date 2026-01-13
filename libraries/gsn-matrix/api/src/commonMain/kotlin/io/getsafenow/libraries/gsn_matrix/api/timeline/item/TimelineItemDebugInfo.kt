package io.getsafenow.libraries.gsn_matrix.api.timeline.item


import kotlinx.serialization.Serializable

@Serializable
data class TimelineItemDebugInfo(
    val model: String,
    val originalJson: String?,
    val latestEditedJson: String?,
)
