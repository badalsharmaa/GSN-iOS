package io.getsafenow.libraries.gsn_matrix.api.exception

/**
 * Exceptions that can occur while resolving the events associated to push notifications.
 */
sealed class NotificationResolverException : Exception() {
    /**
     * The event was not found by the notification service.
     */
    data object EventNotFound : NotificationResolverException()

    /**
     * The event was found but it was filtered out by the notification service.
     */
    data object EventFilteredOut : NotificationResolverException()

    /**
     * An unexpected error occurred while trying to resolve the event.
     */
    data class UnknownError(override val message: String) : NotificationResolverException()
}
