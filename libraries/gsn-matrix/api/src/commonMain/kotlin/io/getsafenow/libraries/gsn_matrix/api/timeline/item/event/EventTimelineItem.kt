package io.getsafenow.libraries.gsn_matrix.api.timeline.item.event

import io.getsafenow.libraries.gsn_matrix.api.timeline.item.EventThreadInfo
import io.getsafenow.libraries.gsn_matrix.api.timeline.item.TimelineItemDebugInfo
import io.getsafenow.libraries.gsn_matrix.api.core.EventId
import io.getsafenow.libraries.gsn_matrix.api.core.SendHandle
import io.getsafenow.libraries.gsn_matrix.api.core.TransactionId
import io.getsafenow.libraries.gsn_matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableList

data class EventTimelineItem(
    val eventId: EventId?,
    val transactionId: TransactionId?,
    val isEditable: Boolean,
    val canBeRepliedTo: Boolean,
    val isOwn: Boolean,
    val isRemote: Boolean,
    val localSendState: LocalEventSendState?,
    val reactions: ImmutableList<EventReaction>,
    val receipts: ImmutableList<Receipt>,
    val sender: UserId,
    val senderProfile: ProfileTimelineDetails,
    val timestamp: Long,
    val content: EventContent,
    val origin: TimelineItemEventOrigin?,
    val timelineItemDebugInfoProvider: TimelineItemDebugInfoProvider,
    val messageShieldProvider: MessageShieldProvider,
    val sendHandleProvider: SendHandleProvider,
) {
    fun inReplyTo(): InReplyTo? {
        return (content as? MessageContent)?.inReplyTo
    }

    fun threadInfo(): EventThreadInfo? = (content as? MessageContent)?.threadInfo

    fun hasNotLoadedInReplyTo(): Boolean {
        val details = inReplyTo()
        return details is InReplyTo.NotLoaded
    }
}

fun interface TimelineItemDebugInfoProvider {
    operator fun invoke(): TimelineItemDebugInfo
}

fun interface MessageShieldProvider {
    operator fun invoke(strict: Boolean): MessageShield?
}

fun interface SendHandleProvider {
    operator fun invoke(): SendHandle?
}
