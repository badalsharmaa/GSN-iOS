package io.getsafenow.libraries.gsn_matrix.api.timeline.item

import io.getsafenow.libraries.architecture.AsyncData
import io.getsafenow.libraries.gsn_matrix.api.core.ThreadId
import io.getsafenow.libraries.gsn_matrix.api.core.UserId
import io.getsafenow.libraries.gsn_matrix.api.timeline.item.event.EventContent
import io.getsafenow.libraries.gsn_matrix.api.timeline.item.event.EventOrTransactionId
import io.getsafenow.libraries.gsn_matrix.api.timeline.item.event.ProfileTimelineDetails

data class EventThreadInfo(
    val threadRootId: ThreadId?,
    val threadSummary: ThreadSummary?,
)

data class ThreadSummary(
    val latestEvent: AsyncData<EmbeddedEventInfo>,
    val numberOfReplies: Long,
)

data class EmbeddedEventInfo(
    val eventOrTransactionId: EventOrTransactionId,
    val content: EventContent,
    val senderId: UserId,
    val senderProfile: ProfileTimelineDetails,
    val timestamp: Long,
)
