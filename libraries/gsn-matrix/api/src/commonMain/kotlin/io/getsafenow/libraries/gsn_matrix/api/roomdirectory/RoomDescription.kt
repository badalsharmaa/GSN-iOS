package io.getsafenow.libraries.gsn_matrix.api.roomdirectory

import io.getsafenow.libraries.gsn_matrix.api.core.RoomAlias
import io.getsafenow.libraries.gsn_matrix.api.core.RoomId


data class RoomDescription(
    val roomId: RoomId,
    val name: String?,
    val topic: String?,
    val alias: RoomAlias?,
    val avatarUrl: String?,
    val joinRule: JoinRule,
    val isWorldReadable: Boolean,
    val numberOfMembers: Long
) {
    enum class JoinRule {
        PUBLIC,
        KNOCK,
        RESTRICTED,
        KNOCK_RESTRICTED,
        INVITE,
        UNKNOWN
    }
}
