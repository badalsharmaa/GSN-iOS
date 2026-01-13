package io.getsafenow.libraries.gsn_matrix.api.room.alias

import io.getsafenow.libraries.gsn_matrix.api.core.RoomAlias

interface RoomAliasHelper {
    fun roomAliasNameFromRoomDisplayName(name: String): String
    fun isRoomAliasValid(roomAlias: RoomAlias): Boolean
}
