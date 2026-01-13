package io.getsafenow.libraries.gsn_matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class EventReaction(
    val key: String,
    val senders: ImmutableList<ReactionSender>
)
