package io.getsafenow.libraries.gsn_matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.getsafenow.libraries.gsn_matrix.api.core.EventId
import io.getsafenow.libraries.gsn_matrix.api.core.UserId

@Immutable
sealed interface InReplyTo {
    /** The event details are not loaded yet. We can fetch them. */
    data class NotLoaded(val eventId: EventId) : InReplyTo

    /** The event details are pending to be fetched. We should **not** fetch them again. */
    data class Pending(val eventId: EventId) : InReplyTo

    /** The event details are available. */
    data class Ready(
        val eventId: EventId,
        val content: EventContent,
        val senderId: UserId,
        val senderProfile: ProfileTimelineDetails,
    ) : InReplyTo

    /**
     * Fetching the event details failed.
     *
     * We can try to fetch them again **with a proper retry strategy**, but not blindly:
     *
     * If the reason for the failure is consistent on the server, we'd enter a loop
     * where we keep trying to fetch the same event.
     * */
    data class Error(
        val eventId: EventId,
        val message: String,
    ) : InReplyTo
}
