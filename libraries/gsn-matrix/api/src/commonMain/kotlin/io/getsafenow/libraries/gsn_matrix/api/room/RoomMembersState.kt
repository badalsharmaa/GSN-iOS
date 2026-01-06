package io.getsafenow.libraries.gsn_matrix.api.room

import androidx.compose.runtime.Immutable
import io.getsafenow.libraries.gsn_matrix.api.core.SessionId
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface RoomMembersState {
    data object Unknown : RoomMembersState
    data class Pending(val prevRoomMembers: ImmutableList<RoomMember>? = null) : RoomMembersState
    data class Error(val failure: Throwable, val prevRoomMembers: ImmutableList<RoomMember>? = null) : RoomMembersState
    data class Ready(val roomMembers: ImmutableList<RoomMember>) : RoomMembersState
}

fun RoomMembersState.roomMembers(): List<RoomMember>? {
    return when (this) {
        is RoomMembersState.Ready -> roomMembers
        is RoomMembersState.Pending -> prevRoomMembers
        is RoomMembersState.Error -> prevRoomMembers
        else -> null
    }
}

fun RoomMembersState.joinedRoomMembers(): List<RoomMember> {
    return roomMembers().orEmpty().filter { it.membership == RoomMembershipState.JOIN }
}

fun RoomMembersState.activeRoomMembers(): List<RoomMember> {
    return roomMembers().orEmpty().filter { it.membership.isActive() }
}

fun RoomMembersState.getDirectRoomMember(roomInfo: RoomInfo, sessionId: SessionId): RoomMember? {
    return roomMembers()
        ?.takeIf { roomInfo.isDm }
        ?.find { it.userId != sessionId && it.membership.isActive() }
}
