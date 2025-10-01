package io.getsafenow.libraries.gsn_matrix.api.poll

enum class PollKind {
    /** Voters should see results as soon as they have voted. */
    Disclosed,

    /** Results should be only revealed when the poll is ended. */
    Undisclosed,
}

val PollKind.isDisclosed: Boolean
    get() = this == PollKind.Disclosed
