package io.getsafenow.libraries.gsn_matrix.api.room

/**
 * Room membership details for the current user and the sender of the membership event.
 *
 * It also includes the reason the current user's membership changed, if any.
 */
data class RoomMembershipDetails(
    val currentUserMember: RoomMember,
    val senderMember: RoomMember?,
) {
    val membershipChangeReason: String? = currentUserMember.membershipChangeReason
}
