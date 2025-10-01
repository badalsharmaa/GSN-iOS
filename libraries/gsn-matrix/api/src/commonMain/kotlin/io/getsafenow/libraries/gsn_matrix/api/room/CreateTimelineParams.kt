package io.getsafenow.libraries.gsn_matrix.api.room


import io.getsafenow.libraries.gsn_matrix.api.core.EventId
import io.getsafenow.libraries.gsn_matrix.api.core.ThreadId

sealed interface CreateTimelineParams {
    data class Focused(val focusedEventId: EventId) : CreateTimelineParams
    data object MediaOnly : CreateTimelineParams
    data class MediaOnlyFocused(val focusedEventId: EventId) : CreateTimelineParams
    data object PinnedOnly : CreateTimelineParams
    data class Threaded(val threadRootEventId: ThreadId) : CreateTimelineParams
}
