package io.getsafenow.libraries.gsn_matrix.api.timeline


import io.getsafenow.libraries.gsn_matrix.api.core.EventId
import io.getsafenow.libraries.gsn_matrix.api.core.TransactionId
import io.getsafenow.libraries.gsn_matrix.api.core.UniqueId
import io.getsafenow.libraries.gsn_matrix.api.timeline.item.event.EventTimelineItem
import io.getsafenow.libraries.gsn_matrix.api.timeline.item.virtual.VirtualTimelineItem

sealed interface MatrixTimelineItem {
    data class Event(val uniqueId: UniqueId, val event: EventTimelineItem) : MatrixTimelineItem {
        val eventId: EventId? = event.eventId
        val transactionId: TransactionId? = event.transactionId
    }

    data class Virtual(val uniqueId: UniqueId, val virtual: VirtualTimelineItem) : MatrixTimelineItem
    data object Other : MatrixTimelineItem
}
