package io.getsafenow.libraries.gsn_matrix.api.timeline.item.event

/**
 * Constants defining known event types from Matrix specifications.
 */
object EventType {
    const val MESSAGE = "m.room.message"

    // Call Events
    const val CALL_INVITE = "m.call.invite"
    const val CALL_NOTIFY = "m.call.notify"
}
