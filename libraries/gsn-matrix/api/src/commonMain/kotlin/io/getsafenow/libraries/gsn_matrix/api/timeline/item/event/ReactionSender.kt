package io.getsafenow.libraries.gsn_matrix.api.timeline.item.event

import io.getsafenow.libraries.gsn_matrix.api.core.UserId

/**
 * The sender of a reaction.
 *
 * @property senderId the ID of the user who sent the reaction
 * @property timestamp the timestamp the reaction was received on the origin homeserver
 */
data class ReactionSender(
    val senderId: UserId,
    val timestamp: Long
)
