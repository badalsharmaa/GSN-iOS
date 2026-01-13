package io.getsafenow.libraries.gsn_matrix.api.room.powerlevels

import io.getsafenow.libraries.gsn_matrix.api.room.BaseRoom
import io.getsafenow.libraries.gsn_matrix.api.room.RoomMember
import io.getsafenow.libraries.gsn_matrix.api.room.RoomMembersState
import io.getsafenow.libraries.gsn_matrix.api.room.activeRoomMembers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Return a flow of the list of active room members who have the given role.
 */
fun BaseRoom.usersWithRole(role: RoomMember.Role): Flow<ImmutableList<RoomMember>> {
    // Ensure the room members flow is ready
    val readyMembersFlow = membersStateFlow
        .onStart {
            if (membersStateFlow.value is RoomMembersState.Unknown) {
                updateMembers()
            }
        }
        .filter { it is RoomMembersState.Ready }

    return roomInfoFlow
        .map { roomInfo -> roomInfo.usersWithRole(role) }
        .combine(readyMembersFlow) { powerLevels, membersState ->
            membersState.activeRoomMembers()
                .filter { powerLevels.contains(it.userId) }
                .toPersistentList()
        }
        .distinctUntilChanged()
}
