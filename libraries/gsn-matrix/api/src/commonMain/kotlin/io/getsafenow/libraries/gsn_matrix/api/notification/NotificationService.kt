package io.getsafenow.libraries.gsn_matrix.api.notification

import io.getsafenow.libraries.gsn_matrix.api.core.EventId
import io.getsafenow.libraries.gsn_matrix.api.core.RoomId

/**
 * Represents the resolution state of an attempt to retrieve notification data for a set of event ids.
 * The outer [Result] indicates the success or failure of the setup to retrieve notifications.
 * The inner [Result] for each [EventId] in the map indicates whether the notification data was successfully retrieved or if there was an error.
 */
typealias GetNotificationDataResult = Result<Map<EventId, Result<NotificationData>>>

/**
 * Service to retrieve notifications for a given set of event ids in specific rooms.
 */
interface NotificationService {
    /**
     * Fetch notifications for the specified event ids in the given rooms.
     */
    suspend fun getNotifications(ids: Map<RoomId, List<EventId>>): GetNotificationDataResult
}
