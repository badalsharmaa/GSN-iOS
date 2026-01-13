package io.getsafenow.libraries.gsn_matrix.api.room.message

import io.getsafenow.libraries.gsn_matrix.api.core.EventId
import io.getsafenow.libraries.gsn_matrix.api.core.UserId
import io.getsafenow.libraries.gsn_matrix.api.timeline.item.event.EventTimelineItem


data class RoomMessage(
    val eventId: EventId,
    val event: EventTimelineItem,
    val sender: UserId,
    val originServerTs: Long,
)
