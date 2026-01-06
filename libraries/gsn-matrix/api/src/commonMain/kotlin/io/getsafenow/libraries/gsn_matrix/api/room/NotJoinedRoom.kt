package io.getsafenow.libraries.gsn_matrix.api.room

import io.getsafenow.libraries.gsn_matrix.api.room.preview.RoomPreviewInfo

/** A reference to a room either invited, knocked or banned. */
interface NotJoinedRoom : AutoCloseable {
    val previewInfo: RoomPreviewInfo
    val localRoom: BaseRoom?

    /**
     * Get the membership details of the user in the room, as well as from the user who sent the `m.room.member` event.
     */
    suspend fun membershipDetails(): Result<RoomMembershipDetails?>
}
