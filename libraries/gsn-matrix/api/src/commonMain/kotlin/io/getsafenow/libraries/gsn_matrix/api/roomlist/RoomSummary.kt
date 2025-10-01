package io.getsafenow.libraries.gsn_matrix.api.roomlist


import io.getsafenow.libraries.gsn_matrix.api.room.RoomInfo
import io.getsafenow.libraries.gsn_matrix.api.room.message.RoomMessage


data class RoomSummary(
    val info: RoomInfo,
    val lastMessage: RoomMessage?,
) {
    val roomId = info.id
    val lastMessageTimestamp = lastMessage?.originServerTs
    val isOneToOne get() = info.activeMembersCount == 2L
}
