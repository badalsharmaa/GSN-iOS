package io.getsafenow.libraries.gsn_matrix.api.roomdirectory

/**
 * Enum class representing the visibility of a room in the room directory.
 */
sealed interface RoomVisibility {
    /**
     * Indicates that the room will be shown in the published room list.
     */
    data object Public : RoomVisibility

    /**
     * Indicates that the room will not be shown in the published room list.
     */
    data object Private : RoomVisibility

    /**
     * A custom value that's not present in the spec.
     */
    data class Custom(val value: String) : RoomVisibility
}
