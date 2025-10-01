package io.getsafenow.libraries.gsn_matrix.api.room.errors

import io.getsafenow.libraries.gsn_matrix.api.core.EventId


sealed class FocusEventException : Exception() {
    data class InvalidEventId(
        val eventId: String,
        val err: String
    ) : FocusEventException()

    data class EventNotFound(
        val eventId: EventId
    ) : FocusEventException()

    data class Other(
        val msg: String
    ) : FocusEventException()
}
