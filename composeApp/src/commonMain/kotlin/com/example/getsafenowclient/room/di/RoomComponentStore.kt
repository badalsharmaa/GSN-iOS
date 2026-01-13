package com.example.getsafenowclient.room.di

import com.example.getsafenowclient.room.RoomComponentImpl
import me.tatarka.inject.annotations.Inject
import net.folivo.trixnity.core.model.RoomId

class RoomComponentStore @Inject constructor() {

    private val components = mutableMapOf<RoomId, RoomComponentImpl>()

    fun get(
        roomId: RoomId,
        factory: () -> RoomComponentImpl
    ): RoomComponentImpl {
        return components.getOrPut(roomId, factory)
    }

    fun clear(roomId: RoomId) {
        components.remove(roomId)
    }

    fun clearAll() {
        components.clear()
    }
}
