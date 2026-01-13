

package io.getsafenow.libraries.gsn_matrix.api.room.alias

import io.getsafenow.libraries.gsn_matrix.api.room.BaseRoom
import io.getsafenow.libraries.gsn_matrix.api.core.RoomIdOrAlias


/**
 * Return true if the given roomIdOrAlias is the same room as this room.
 */
fun BaseRoom.matches(roomIdOrAlias: RoomIdOrAlias): Boolean {
    return when (roomIdOrAlias) {
        is RoomIdOrAlias.Id -> {
            roomIdOrAlias.roomId == roomId
        }
        is RoomIdOrAlias.Alias -> {
            roomIdOrAlias.roomAlias == info().canonicalAlias || roomIdOrAlias.roomAlias in info().alternativeAliases
        }
    }
}
