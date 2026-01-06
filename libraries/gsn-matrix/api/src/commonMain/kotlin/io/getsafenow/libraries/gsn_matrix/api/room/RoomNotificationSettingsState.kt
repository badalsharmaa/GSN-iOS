package io.getsafenow.libraries.gsn_matrix.api.room

sealed interface RoomNotificationSettingsState {
    data object Unknown : RoomNotificationSettingsState
    data class Pending(val prevRoomNotificationSettings: RoomNotificationSettings? = null) : RoomNotificationSettingsState
    data class Error(val failure: Throwable, val prevRoomNotificationSettings: RoomNotificationSettings? = null) : RoomNotificationSettingsState
    data class Ready(val roomNotificationSettings: RoomNotificationSettings) : RoomNotificationSettingsState
}

fun RoomNotificationSettingsState.roomNotificationSettings(): RoomNotificationSettings? {
    return when (this) {
        is RoomNotificationSettingsState.Ready -> roomNotificationSettings
        is RoomNotificationSettingsState.Pending -> prevRoomNotificationSettings
        is RoomNotificationSettingsState.Error -> prevRoomNotificationSettings
        else -> null
    }
}
