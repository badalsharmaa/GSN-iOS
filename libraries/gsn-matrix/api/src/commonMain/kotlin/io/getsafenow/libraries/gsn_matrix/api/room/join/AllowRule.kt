package io.getsafenow.libraries.gsn_matrix.api.room.join

import androidx.compose.runtime.Immutable
import io.getsafenow.libraries.gsn_matrix.api.core.RoomId


@Immutable
sealed interface AllowRule {
    data class RoomMembership(val roomId: RoomId) : AllowRule
    data class Custom(val json: String) : AllowRule
}
