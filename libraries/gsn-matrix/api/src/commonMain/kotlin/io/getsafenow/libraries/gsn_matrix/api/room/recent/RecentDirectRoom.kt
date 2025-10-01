package io.getsafenow.libraries.gsn_matrix.api.room.recent

import io.getsafenow.libraries.gsn_matrix.api.room.BaseRoom
import io.getsafenow.libraries.gsn_matrix.api.room.CurrentUserMembership
import io.getsafenow.libraries.gsn_matrix.api.GsnMClient
import io.getsafenow.libraries.gsn_matrix.api.core.RoomId
import io.getsafenow.libraries.gsn_matrix.api.core.UserId
import io.getsafenow.libraries.gsn_matrix.api.user.GsnMUser


private const val MAX_RECENT_DIRECT_ROOMS_TO_RETURN = 5

data class RecentDirectRoom(
    val roomId: RoomId,
    val matrixUser: GsnMUser,
)

suspend fun GsnMClient.getRecentDirectRooms(
    maxNumberOfResults: Int = MAX_RECENT_DIRECT_ROOMS_TO_RETURN,
): List<RecentDirectRoom> {
    val result = mutableListOf<RecentDirectRoom>()
    val foundUserIds = mutableSetOf<UserId>()
    getRecentlyVisitedRooms().getOrNull()?.let { roomIds ->
        roomIds
            .mapNotNull { roomId -> getRoom(roomId) }
            .filter { it.isDm() && it.isJoined() }
            .map { room ->
                val otherUser = room.getMembers().getOrNull()
                    ?.firstOrNull { it.userId != sessionId }
                    ?.takeIf { foundUserIds.add(it.userId) }
                    ?.toMatrixUser()
                if (otherUser != null) {
                    result.add(
                        RecentDirectRoom(room.roomId, otherUser)
                    )
                    // Return early to avoid useless computation
                    if (result.size >= maxNumberOfResults) {
                        return@map
                    }
                }
            }
    }
    return result
}

suspend fun BaseRoom.isJoined(): Boolean {
    return roomInfoFlow.first().currentUserMembership == CurrentUserMembership.JOINED
}
