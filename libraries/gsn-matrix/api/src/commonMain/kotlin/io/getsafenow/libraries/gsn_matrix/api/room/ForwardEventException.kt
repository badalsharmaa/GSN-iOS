package io.getsafenow.libraries.gsn_matrix.api.room

import io.getsafenow.libraries.gsn_matrix.api.core.RoomId


class ForwardEventException(
    val roomIds: List<RoomId>
) : Exception() {
    override val message: String? = "Failed to deliver event to $roomIds rooms"
}
