package io.getsafenow.libraries.gsn_matrix.api.timeline

sealed class TimelineException : Exception() {
    data object CannotPaginate : TimelineException()
    data object EventNotFound : TimelineException()
}
