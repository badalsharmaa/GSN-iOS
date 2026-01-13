package com.example.getsafenowclient.common.events.membership

import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.Membership

object MembershipFormatter {

    fun format(
        membership: Membership,
        sender: UserId,
        target: UserId,
        senderUser: RoomUser?,
        targetUser: RoomUser?,
        currentUserId: UserId
    ): String {

        val senderName = senderUser?.name ?: sender.localpart
        val targetName = targetUser?.name ?: target.localpart

        val senderFriendly = if (sender == currentUserId) "You" else senderName
        val targetFriendly = if (target == currentUserId) "You" else targetName

        return when (membership) {
            Membership.JOIN -> "$targetFriendly: Joined the chat."
            Membership.LEAVE ->
                if (sender == target) "$targetFriendly left the chat."
                else "$senderFriendly removed $targetFriendly"
            Membership.INVITE -> "$senderFriendly invited $targetFriendly"
            Membership.BAN -> "$senderFriendly removed $targetFriendly"
            Membership.KNOCK -> "$targetFriendly requested to join the chat."
            else -> "$targetFriendly updated membership"
        }
    }
}
