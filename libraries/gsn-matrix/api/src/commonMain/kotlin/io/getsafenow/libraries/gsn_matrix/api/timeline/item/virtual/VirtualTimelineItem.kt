package io.getsafenow.libraries.gsn_matrix.api.timeline.item.virtual

import io.getsafenow.libraries.gsn_matrix.api.timeline.Timeline

sealed interface VirtualTimelineItem {
    data class DayDivider(
        val timestamp: Long
    ) : VirtualTimelineItem

    data object ReadMarker : VirtualTimelineItem

    data object RoomBeginning : VirtualTimelineItem

    data object LastForwardIndicator : VirtualTimelineItem

    data class LoadingIndicator(
        val direction: Timeline.PaginationDirection,
        val timestamp: Long,
    ) : VirtualTimelineItem

    data object TypingNotification : VirtualTimelineItem
}
