package io.getsafenow.libraries.gsn_matrix.api.room

data class RoomNotificationSettings(
    val mode: RoomNotificationMode,
    val isDefault: Boolean,
)

enum class RoomNotificationMode {
    ALL_MESSAGES,
    MENTIONS_AND_KEYWORDS_ONLY,
    MUTE
}
