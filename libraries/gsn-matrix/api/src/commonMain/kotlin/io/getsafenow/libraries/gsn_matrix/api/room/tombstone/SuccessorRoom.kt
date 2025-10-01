package io.getsafenow.libraries.gsn_matrix.api.room.tombstone

import io.getsafenow.libraries.gsn_matrix.api.core.RoomId

/**
 *
 * When a room A is tombstoned, it is replaced by a room B. The room A is the
 * predecessor of B, and B is the successor of A. This type holds information
 * about the successor room.
 *
 * A room is tombstoned if it has received a m.room.tombstone state event.
 *
 */
data class SuccessorRoom(
    /**
     * The ID of the replacement room.
     */
    val roomId: RoomId,
    /**
     * The message explaining why the room has been tombstoned.
     */
    val reason: String?,
)
