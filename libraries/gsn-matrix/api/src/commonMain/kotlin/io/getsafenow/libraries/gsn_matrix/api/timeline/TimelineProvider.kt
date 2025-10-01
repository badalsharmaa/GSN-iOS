package io.getsafenow.libraries.gsn_matrix.api.timeline

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/**
 * This interface defines a way to get the active timeline.
 * It could be the live timeline, a pinned timeline or a detached timeline.
 * By default, the active timeline is the live timeline.
 */
interface TimelineProvider {
    fun activeTimelineFlow(): StateFlow<Timeline?>
}

suspend fun TimelineProvider.getActiveTimeline(): Timeline = activeTimelineFlow().filterNotNull().first()
