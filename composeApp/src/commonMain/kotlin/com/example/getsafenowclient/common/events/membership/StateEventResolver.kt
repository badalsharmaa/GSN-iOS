package com.example.getsafenowclient.common.events.membership

import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.store.TimelineEvent
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.ClientEvent
import net.folivo.trixnity.core.model.events.StateEventContent
import net.folivo.trixnity.core.model.events.UnsignedRoomEventData
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent

object StateEventResolver {

    /**
     * Converts *any* StateEventContent into a readable system message.
     */
    fun resolve(
        roomId: RoomId,
        timelineEvent: TimelineEvent,
        currentUserId: UserId,
        senderUser: RoomUser?,
        targetUser: RoomUser?
    ): String? {
        val decrypted = timelineEvent.content?.getOrNull() ?: return null

        return when (decrypted) {

            is MemberEventContent -> {
                val stateEvent =
                    timelineEvent.event as? ClientEvent.RoomEvent.StateEvent<*> ?: return null

                val targetId = stateEvent.stateKey
                    .takeIf { it.isNotEmpty() }
                    ?.let { UserId(it) }
                    ?: timelineEvent.event.sender

                // ---------- NEW: read previousContent ----------
                val unsigned = stateEvent.unsigned
                val prevContent = unsigned?.previousContent as? MemberEventContent

                // Friendly naming
                val senderName = senderUser?.name ?: stateEvent.sender.localpart
                val targetName = targetUser?.name ?: targetId.localpart

                val senderFriendly =
                    if (stateEvent.sender == currentUserId) "You" else senderName
                val targetFriendly =
                    if (targetId == currentUserId) "You" else targetName

                // If we have previous content, we can detect what changed
                if (prevContent != null) {
                    val nameChanged =
                        prevContent.displayName != decrypted.displayName
                    val avatarChanged =
                        prevContent.avatarUrl != decrypted.avatarUrl
                    val membershipChanged =
                        prevContent.membership != decrypted.membership

                    return when {
                        // 1) Actual membership change (join/leave/invite/ban…)
                        membershipChanged -> {
                            MembershipFormatter.format(
                                membership = decrypted.membership,
                                sender = stateEvent.sender,
                                target = targetId,
                                senderUser = senderUser,
                                targetUser = targetUser,
                                currentUserId = currentUserId
                            )
                        }

                        // 2) Profile changes only
                        nameChanged && !avatarChanged ->
                            "$targetFriendly changed display name"

                        avatarChanged && !nameChanged ->
                            "$targetFriendly changed profile picture"

                        nameChanged && avatarChanged ->
                            "$targetFriendly updated profile"

                        // 3) Nothing interesting changed – ignore
                        else -> null
                    }
                }

                // No previousContent → legacy path: likely first join, invite, etc.
                MembershipFormatter.format(
                    membership = decrypted.membership,
                    sender = stateEvent.sender,
                    target = targetId,
                    senderUser = senderUser,
                    targetUser = targetUser,
                    currentUserId = currentUserId
                )
            }

            is StateEventContent -> "[${decrypted::class.simpleName}]"
            else -> null
        }
    }
}


