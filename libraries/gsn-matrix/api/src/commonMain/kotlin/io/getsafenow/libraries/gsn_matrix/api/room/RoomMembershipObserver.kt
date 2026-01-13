package io.getsafenow.libraries.gsn_matrix.api.room


import io.getsafenow.libraries.gsn_matrix.api.core.RoomId
import io.getsafenow.libraries.gsn_matrix.api.timeline.item.event.MembershipChange
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RoomMembershipObserver {
    data class RoomMembershipUpdate(
        val roomId: RoomId,
        val isUserInRoom: Boolean,
        val change: MembershipChange,
    )

    private val _updates = MutableSharedFlow<RoomMembershipUpdate>(extraBufferCapacity = 10)
    val updates = _updates.asSharedFlow()

    suspend fun notifyUserLeftRoom(roomId: RoomId, membershipBeforeLeft: CurrentUserMembership) {
        val membershipChange = when (membershipBeforeLeft) {
            CurrentUserMembership.INVITED -> MembershipChange.INVITATION_REJECTED
            CurrentUserMembership.KNOCKED -> MembershipChange.KNOCK_RETRACTED
            else -> MembershipChange.LEFT
        }
        _updates.emit(RoomMembershipUpdate(roomId, false, membershipChange))
    }
}
