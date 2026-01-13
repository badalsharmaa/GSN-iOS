package io.getsafenow.libraries.gsn_matrix.api.room.join

import io.getsafenow.libraries.gsn_matrix.api.room.JoinedRoom
import io.getsafenow.libraries.gsn_matrix.api.core.RoomIdOrAlias


interface JoinRoom {
    suspend operator fun invoke(
        roomIdOrAlias: RoomIdOrAlias,
        serverNames: List<String>,
        trigger: String? = null,
    ): Result<Unit>

    sealed class Failures : Exception() {
        data object UnauthorizedJoin : Failures()
    }
}
