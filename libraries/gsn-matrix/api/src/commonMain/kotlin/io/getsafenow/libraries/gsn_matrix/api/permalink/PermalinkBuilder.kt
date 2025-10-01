package io.getsafenow.libraries.gsn_matrix.api.permalink

import io.getsafenow.libraries.gsn_matrix.api.core.RoomAlias
import io.getsafenow.libraries.gsn_matrix.api.core.UserId


interface PermalinkBuilder {
    fun permalinkForUser(userId: UserId): Result<String>
    fun permalinkForRoomAlias(roomAlias: RoomAlias): Result<String>
}

sealed class PermalinkBuilderError : Throwable() {
    data object InvalidData : PermalinkBuilderError()
}
