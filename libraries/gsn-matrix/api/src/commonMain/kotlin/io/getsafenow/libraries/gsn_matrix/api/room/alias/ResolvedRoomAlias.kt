package io.getsafenow.libraries.gsn_matrix.api.room.alias

import io.getsafenow.libraries.gsn_matrix.api.core.RoomId

/**
 * Information about a room, that was resolved from a room alias.
 */
data class ResolvedRoomAlias(
    /**
     * The room ID that the alias resolved to.
     */
    val roomId: RoomId,
    /**
     * A list of servers that can be used to find the room by its room ID.
     */
    val servers: List<String>
)
