package io.getsafenow.libraries.gsn_matrix.api.room.tombstone

import io.getsafenow.libraries.gsn_matrix.api.core.RoomId

/**
 *
 * When a room A is tombstoned, it is replaced by a room B. The room A is the
 * predecessor of B, and B is the successor of A. This type holds information
 * about the predecessor room.
 *
 * A room is tombstoned if it has received a m.room.tombstone state event.
 */
data class PredecessorRoom(
    /**
     * The ID of the replaced room.
     */
    val roomId: RoomId,
)
