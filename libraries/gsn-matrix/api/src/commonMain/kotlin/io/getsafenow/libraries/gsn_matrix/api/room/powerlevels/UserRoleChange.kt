package io.getsafenow.libraries.gsn_matrix.api.room.powerlevels

import io.getsafenow.libraries.gsn_matrix.api.room.RoomMember
import io.getsafenow.libraries.gsn_matrix.api.core.UserId


data class UserRoleChange(
    val userId: UserId,
    val role: RoomMember.Role,
) {
    val powerLevel: Long = role.powerLevel
}
